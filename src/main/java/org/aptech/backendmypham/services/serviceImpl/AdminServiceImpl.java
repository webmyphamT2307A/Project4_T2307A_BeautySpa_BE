package org.aptech.backendmypham.services.serviceImpl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.UserRequestDto;
import org.aptech.backendmypham.models.Role;
import org.aptech.backendmypham.models.User;
import org.aptech.backendmypham.repositories.RoleRepository;
import org.aptech.backendmypham.repositories.UserRepository;
import org.aptech.backendmypham.services.AdminService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    // Sử dụng thread pool với 2 threads (đủ để xử lý đồng thời mà không gây quá tải)
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

   @Override
    public void createAdmin(UserRequestDto userRequestDto) {
        if (userRequestDto.getPassword() == null || userRequestDto.getEmail() == null || userRequestDto.getPhone() == null || userRequestDto.getAddress() == null) {
            throw new RuntimeException("Thông tin không được để trống!");
        }

        CompletableFuture<Optional<Role>> roleFuture = CompletableFuture.supplyAsync(() -> roleRepository.findById((long) userRequestDto.getRoleId()));

        CompletableFuture<Optional<User>> emailFuture = CompletableFuture.supplyAsync(() -> userRepository.findByEmail(userRequestDto.getEmail()));
        CompletableFuture<Optional<User>> phoneFuture = CompletableFuture.supplyAsync(() -> userRepository.findByPhone(userRequestDto.getPhone()));

        try {
            Optional<Role> roleOpt = roleFuture.get(5, TimeUnit.SECONDS);
            Optional<User> emailOpt = emailFuture.get(5, TimeUnit.SECONDS);
            Optional<User> phoneOpt = phoneFuture.get(5, TimeUnit.SECONDS);

            if (roleOpt.isEmpty()) {
                throw new RuntimeException("Role không tồn tại!");
            }


            if (emailOpt.isPresent()) {
                throw new RuntimeException("Email đã tồn tại!");
            }

            if (phoneOpt.isPresent()) {
                throw new RuntimeException("Số điện thoại đã tồn tại!");
            }

            // Tạo mới User
            User user = new User();
            user.setFullName(userRequestDto.getFullName().trim());
            user.setPassword(passwordEncoder.encode(userRequestDto.getPassword().trim()));
            user.setEmail(userRequestDto.getEmail().trim());
            user.setPhone(userRequestDto.getPhone().trim());
            user.setAddress(userRequestDto.getAddress().trim());
            user.setImageUrl(userRequestDto.getImageUrl().trim());
            user.setIsActive(1);
            user.setRole(roleOpt.get());
            user.setCreatedAt(Instant.now());
            userRepository.save(user);

        } catch (TimeoutException e) {
            throw new RuntimeException("Một trong các yêu cầu kiểm tra dữ liệu mất quá nhiều thời gian. Vui lòng thử lại sau.");
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Lỗi khi kiểm tra tồn tại của role: " + e.getMessage());
        }
    }


    @Override
    @Transactional
    public void updateAdmin(Long userId, String fullName, String password, String email, String phoneNumber, String address, Integer roleId,String imageUrl,Integer isActive) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Người dùng không tồn tại!");
        }
        User user = userOpt.get();

        // Cập nhật fullName
        if (fullName != null) {
            user.setFullName(fullName);
        }

        if (password != null) {
            user.setPassword(passwordEncoder.encode(password));
        }

        // Sửa logic kiểm tra email
        if (email != null && !email.equals(user.getEmail())) {
            Optional<User> emailOpt = userRepository.findByEmail(email);
            if (emailOpt.isPresent()) {
                throw new RuntimeException("Email đã tồn tại!");
            }
            user.setEmail(email);
        }
        if (imageUrl != null) {
            user.setImageUrl(imageUrl);
        }

        // Sửa logic kiểm tra phone
        if (phoneNumber != null && !phoneNumber.equals(user.getPhone())) {
            Optional<User> phoneOpt = userRepository.findByPhone(phoneNumber);
            if (phoneOpt.isPresent()) {
                throw new RuntimeException("Số điện thoại đã tồn tại!");
            }
            user.setPhone(phoneNumber);
        }

        if (address != null) {
            user.setAddress(address);
        }

        if (roleId != null) {
            Optional<Role> roleOpt = roleRepository.findById((long) roleId);
            if (roleOpt.isEmpty()) {
                throw new RuntimeException("Role không tồn tại!");
            }
            user.setRole(roleOpt.get());
        }


        if (isActive != null) {
            user.setIsActive(isActive);
        }

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteAdmin(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new RuntimeException("Người dùng không tồn tại!");
        }



        user.setIsActive(-1);
        userRepository.save(user);
    }

    // Hàm lấy kết quả với timeout (không đổi)
    private <T> T getFutureResultWithTimeout(Future<T> future, String entityName, int seconds)
            throws InterruptedException, ExecutionException {
        try {
            return future.get(seconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new RuntimeException("Tìm kiếm " + entityName + " quá thời gian. Vui lòng thử lại sau.");
        }
    }

    @Override
    public User findById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public User findByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhone(phoneNumber).orElse(null);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAllActiveAndInactive();
    }
     @Override
    public  List<User> findALlDeteleted(){return userRepository.findAllDeleted();}


}