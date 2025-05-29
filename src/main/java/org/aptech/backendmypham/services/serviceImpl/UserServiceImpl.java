package org.aptech.backendmypham.services.serviceImpl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.TechnicianResponseDTO;
import org.aptech.backendmypham.dto.TechnicianSearchCriteriaDTO;
import org.aptech.backendmypham.models.Branch;
import org.aptech.backendmypham.models.Role;
import org.aptech.backendmypham.models.User;
import org.aptech.backendmypham.repositories.BranchRepository;
import org.aptech.backendmypham.repositories.RoleRepository;
import org.aptech.backendmypham.repositories.UserRepository;
import org.aptech.backendmypham.repositories.specifications.UserSpecifications;
import org.aptech.backendmypham.services.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final BranchRepository branchRepository;

    // Sử dụng thread pool với 2 threads (đủ để xử lý đồng thời mà không gây quá tải)
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    @Override
    public void createUser(String password,String fullName, String email, String phoneNumber, String address, Integer roleId, Integer branchId) {
        try {
            if (password == null || email == null || phoneNumber == null || address == null) {
                throw new RuntimeException("Thông tin không được để trống!");
            }

            // Mặc định gán roleId = 4 cho customer nếu không truyền roleId
            final Integer finalRoleId = (roleId != null) ? roleId : 4; // Role là 4 nếu không có roleId truyền vào

            // Bất đồng bộ để kiểm tra tồn tại của role
            Future<Optional<Role>> roleFuture = executor.submit(() -> roleRepository.findById((long) finalRoleId));

            // Bất đồng bộ để kiểm tra email & số điện thoại
            final String finalEmail = email;
            final String finalPhoneNumber = phoneNumber;
            Future<Optional<User>> emailFuture = executor.submit(() -> userRepository.findByEmail(finalEmail));
            Future<Optional<User>> phoneFuture = executor.submit(() -> userRepository.findByPhone(finalPhoneNumber));

            // Nếu có branchId thì tìm branch (bất đồng bộ)
            final Integer finalBranchId = branchId;
            Future<Optional<Branch>> branchFuture = (finalBranchId != null)
                    ? executor.submit(() -> branchRepository.findById((long) finalBranchId))
                    : null;

            // Lấy kết quả (timeout 3 giây)
            Optional<Role> roleOpt = getFutureResultWithTimeout(roleFuture, "role", 3);
            Optional<Branch> branchOpt = (branchFuture != null) ? getFutureResultWithTimeout(branchFuture, "chi nhánh", 3) : Optional.empty();
            Optional<User> emailOpt = getFutureResultWithTimeout(emailFuture, "email", 3);
            Optional<User> phoneOpt = getFutureResultWithTimeout(phoneFuture, "số điện thoại", 3);

            // Kiểm tra dữ liệu hợp lệ
            if (roleOpt.isEmpty()) throw new RuntimeException("Role không tồn tại!");
            if (branchFuture != null && branchOpt.isEmpty()) throw new RuntimeException("Chi nhánh không tồn tại!");
            if (emailOpt.isPresent()) throw new RuntimeException("Email đã tồn tại!");
            if (phoneOpt.isPresent()) throw new RuntimeException("Số điện thoại đã tồn tại!");

            // Tạo user mới
            User user = new User();
            user.setFullName(fullName);
            user.setPassword(passwordEncoder.encode(password));
            user.setEmail(finalEmail);
            user.setPhone(finalPhoneNumber);
            user.setAddress(address);
            user.setRole(roleOpt.get());
            user.setIsActive(true);
            branchOpt.ifPresent(user::setBranch);


            userRepository.save(user);

        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Lỗi khi kiểm tra tồn tại của role và branch: " + e.getMessage());
        }
    }

    @Override
    public void updateUser(Long id, String password, String fullName, String email, String phoneNumber, String address, Integer roleId, Integer branchId) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                throw new RuntimeException("Người dùng không tồn tại!");
            }

            User user = userOpt.get();

            Optional<User> phoneOpt = userRepository.findByPhone(phoneNumber);
            if (phoneOpt.isPresent() && !phoneOpt.get().getId().equals(user.getId())) {
                throw new RuntimeException("Số điện thoại đã tồn tại!");
            }

            final Integer finalRoleId = (roleId != null) ? roleId : 4;
            Optional<Role> roleOpt = roleRepository.findById((long) finalRoleId);
            if (roleOpt.isEmpty()) {
                throw new RuntimeException("Role không tồn tại!");
            }

            Optional<Branch> branchOpt = (branchId != null) ? branchRepository.findById((long) branchId) : Optional.empty();
            if (branchId != null && branchOpt.isEmpty()) {
                throw new RuntimeException("Chi nhánh không tồn tại!");
            }

            user.setFullName(fullName);
            if (password != null && !password.isEmpty()) {
                user.setPassword(passwordEncoder.encode(password));
            }
            user.setPhone(phoneNumber);
            user.setAddress(address);
            user.setRole(roleOpt.get());
            branchOpt.ifPresent(user::setBranch);

            userRepository.save(user);

        } catch (RuntimeException e) {
            throw new RuntimeException("Lỗi khi cập nhật thông tin người dùng: " + e.getMessage());
        }
    }
    @Transactional
    @Override
    public void deleteUser(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Người dùng không tồn tại!");
        }

        User user = userOpt.get();

        if (user.getRole().getId() != 4) {
            throw new RuntimeException("Chỉ có thể vô hiệu hóa tài khoản khách hàng!");
        }

        user.setIsActive(false);
        userRepository.save(user);
    }
    public List<User> getUsersByRole(String roleName) {
        return userRepository.findByRoleName(roleName);
    }
    // Hàm lấy kết quả với timeout (không đổi)

    @Override
    public Page<TechnicianResponseDTO> findTechnicians(TechnicianSearchCriteriaDTO criteria, Pageable pageable) {
        // Luôn lọc theo role "TECHNICIAN" (hoặc tên role bạn định nghĩa cho kỹ thuật viên) và isActive
        // Ví dụ: final String TECHNICIAN_ROLE_NAME = "TECHNICIAN";
        final String TECHNICIAN_ROLE_NAME = "staff"; // HOẶC TÊN ROLE KỸ THUẬT VIÊN CỦA BẠN

        Specification<User> spec = Specification.where(UserSpecifications.isActive())
                .and(UserSpecifications.hasRole(TECHNICIAN_ROLE_NAME));

        if (!CollectionUtils.isEmpty(criteria.getSkillIds())) {
            // Logic cho requireAllSkills:
            // Nếu requireAllSkills = true, bạn có thể cần một query phức tạp hơn hoặc lọc sau.
            // Hiện tại, UserSpecifications.hasAllSkills đang hoạt động tương tự hasAnyOfSkills
            // để đơn giản. Bạn cần quyết định cách xử lý "AND skills" chính xác.
            // Ví dụ:
            // if (Boolean.TRUE.equals(criteria.getRequireAllSkills())) {
            //     // Cần một specification phức tạp hơn cho "has ALL skills"
            //     // Hoặc gọi một phương thức repository riêng với @Query tùy chỉnh
            // } else {
            //     spec = spec.and(UserSpecifications.hasAnyOfSkills(criteria.getSkillIds()));
            // }
            // Tạm thời dùng hasAnyOfSkills cho cả hai, client/FE có thể filter thêm nếu cần độ chính xác tuyệt đối cho "ALL"
            spec = spec.and(UserSpecifications.hasAnyOfSkills(criteria.getSkillIds()));

        }

        if (criteria.getSkillKeyword() != null && !criteria.getSkillKeyword().trim().isEmpty()) {
            spec = spec.and(UserSpecifications.skillKeywordSearch(criteria.getSkillKeyword()));
        }

        if (criteria.getMinAverageRating() != null) {
            spec = spec.and(UserSpecifications.hasMinimumAverageRating(criteria.getMinAverageRating()));
        }

        if (criteria.getMinTotalReviews() != null) {
            spec = spec.and(UserSpecifications.hasMinimumTotalReviews(criteria.getMinTotalReviews()));
        }

        Page<User> userPage = userRepository.findAll(spec, pageable);

        // Map Page<User> sang Page<TechnicianResponseDTO>
        return userPage.map(TechnicianResponseDTO::new); // Sử dụng constructor của DTO
    }
    private <T> T getFutureResultWithTimeout(Future<T> future, String entityName, int seconds)
            throws InterruptedException, ExecutionException {
        try {
            return future.get(seconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new RuntimeException("Tìm kiếm " + entityName + " quá thời gian. Vui lòng thử lại sau.");
        }
    }
}

