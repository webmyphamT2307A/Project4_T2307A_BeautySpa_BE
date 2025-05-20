package org.aptech.backendmypham.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.configs.JwtService;
import org.aptech.backendmypham.dto.*;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.models.Branch;
import org.aptech.backendmypham.models.Role;
import org.aptech.backendmypham.models.User;
import org.aptech.backendmypham.repositories.BranchRepository;
import org.aptech.backendmypham.repositories.RoleRepository;
import org.aptech.backendmypham.repositories.UserRepository;
import org.aptech.backendmypham.services.userDetailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class userDetailServiceImpl implements userDetailService {
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private static final Logger logger = LoggerFactory.getLogger(userDetailServiceImpl.class);
    private final RoleRepository roleRepository;
    @Override
    public ResponseObject registerUser(UserRegisterDto userRegisterDto) {
        // Kiểm tra nếu email đã tồn tại
        if (userRepository.existsByEmail(userRegisterDto.getEmail())) {
            return new ResponseObject(Status.ERROR, "Email đã được đăng ký", null);
        }

        // Mã hóa mật khẩu
        String encodedPassword = passwordEncoder.encode(userRegisterDto.getPassword());
        // Lấy đối tượng Branch từ branchId
        Branch branch = branchRepository.findById(userRegisterDto.getBranchId())
                .orElseThrow(() -> new RuntimeException("Nhánh không tồn tại"));

        Role role = roleRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Vai trò không tồn tại"));

        // Tạo đối tượng người dùng
        User user = new User();
        user.setFullName(userRegisterDto.getFullName());
        user.setEmail(userRegisterDto.getEmail());
        user.setPassword(encodedPassword);
        user.setPhone(userRegisterDto.getPhone());
        user.setAddress(userRegisterDto.getAddress());
        user.setBranch(branch);
        user.setIsActive(true);
        user.setRole(role);

        // Lưu người dùng vào cơ sở dữ liệu
        User savedUser = userRepository.save(user);

        // Trả về đối tượng ResponseObject với thông tin người dùng mới đã đăng ký
        return new ResponseObject(Status.SUCCESS, "Đăng ký thành công", savedUser);
    }
    @Override
    public ResponseObject login(LoginRequestDto dto) {


        // Tìm người dùng trong cơ sở dữ liệu
        Optional<User> userOptional = userRepository.findByEmail(dto.getEmail());
        if (userOptional.isEmpty()) {
            return new ResponseObject(Status.ERROR, "Email không tồn tại", null);
        }
        User user = userOptional.get();

        // Log mật khẩu người dùng nhập vào và mật khẩu đã lưu trong cơ sở dữ liệu
        System.out.println("Match? " + passwordEncoder.matches(dto.getPassword(), user.getPassword()));

        // So sánh mật khẩu
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            logger.error("Mật khẩu không đúng cho email: {}", dto.getEmail());
            throw new RuntimeException("Mật khẩu không đúng");

        }


        // Nếu mật khẩu đúng, tạo token
        String token = jwtService.generateTokenForUser(user);
        return new ResponseObject(Status.SUCCESS, "Đăng nhập thành công", Map.of(
                "user", user,
                "token", token
        ));

    }



    @Override
    public ResponseObject logout() {
        return new ResponseObject(Status.SUCCESS, "Đăng xuất thành công", null);
    }

    @Override
    public ResponseObject getUserDetail(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        return new ResponseObject(Status.SUCCESS, "Thông tin người dùng", user);
    }

    @Override
    public ResponseObject updateInfo(Long id, UserInfoUpdateDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        user.setFullName(dto.getFullName());
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());

        userRepository.save(user);
        return new ResponseObject(Status.SUCCESS, "Cập nhật thông tin thành công", user);
    }

    @Override
    public ResponseObject changePassword(Long id, UserPasswordChangeDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không đúng");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        return new ResponseObject(Status.SUCCESS, "Đổi mật khẩu thành công", null);
    }



}
