package org.aptech.backendmypham.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.*;
import org.aptech.backendmypham.models.Customer;
import org.aptech.backendmypham.repositories.CustomerRepository;
import org.aptech.backendmypham.services.CustomerService;
import org.aptech.backendmypham.configs.JwtService;
import org.aptech.backendmypham.enums.Status;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public ResponseObject registerCustomer(RegisterRequestDto registerRequestDto) {
        // Kiểm tra nếu email đã tồn tại
        if (customerRepository.existsByEmail(registerRequestDto.getEmail())) {
            return new ResponseObject(Status.ERROR, "Email đã được đăng ký", null);
        }

        // Mã hóa mật khẩu
        String encodedPassword = passwordEncoder.encode(registerRequestDto.getPassword());

        // Tạo đối tượng customer
        Customer customer = new Customer();
        customer.setFullName(registerRequestDto.getFullName());
        customer.setEmail(registerRequestDto.getEmail());
        customer.setPassword(encodedPassword);
        customer.setPhone(registerRequestDto.getPhone());
        customer.setAddress(registerRequestDto.getAddress());
        customer.setCreatedAt(Instant.now());
        customer.setIsActive(true);

        // Lưu customer vào cơ sở dữ liệu
        customerRepository.save(customer);

        return new ResponseObject(Status.SUCCESS, "Đăng ký thành công", customer);
    }

    @Override
    public ResponseObject loginCustomer(LoginCustomerDto loginCustomerDto) {
        // Kiểm tra nếu email tồn tại
        Customer customer = customerRepository.findByEmail(loginCustomerDto.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        // Kiểm tra mật khẩu
        if (!passwordEncoder.matches(loginCustomerDto.getPassword(), customer.getPassword())) {
            throw new RuntimeException("Mật khẩu không đúng");
        }

        // Tạo token
        String token = jwtService.generateTokenForCustomer(customer);

        return new ResponseObject(Status.SUCCESS, "Đăng nhập thành công", Map.of(
                "customer", customer,
                "token", token
        ));
    }

    @Override
    public ResponseObject getCustomerDetail(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        return new ResponseObject(Status.SUCCESS, "Thông tin người dùng", customer);
    }

    @Override
    public ResponseObject updateCustomer(Long id, CustomerDetailResponseDto customerDetailResponseDto, MultipartFile file) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Cập nhật thông tin cơ bản
        customer.setFullName(customerDetailResponseDto.getFullName());
        customer.setPhone(customerDetailResponseDto.getPhone());
        customer.setAddress(customerDetailResponseDto.getAddress());

        // Nếu có file thì lưu avatar
        if (file != null && !file.isEmpty()) {
            try {
                String uploadDir = "uploads/";
                java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);
                if (!java.nio.file.Files.exists(uploadPath)) {
                    java.nio.file.Files.createDirectories(uploadPath);
                }

                // Kiểm tra loại file
                String fileType = file.getContentType();
                if (!fileType.startsWith("image/")) {
                    throw new RuntimeException("Chỉ cho phép tải lên ảnh.");
                }

                // Tạo tên file duy nhất: yyyyMMddHHmmssSSS_originalName
                String timePrefix = java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")
                        .format(java.time.LocalDateTime.now());
                String fileName = timePrefix + "_" + file.getOriginalFilename().replaceAll("\\s+", "_");
                java.nio.file.Path filePath = uploadPath.resolve(fileName);
                java.nio.file.Files.copy(file.getInputStream(), filePath);

                customer.setImageUrl("/uploads/" + fileName);
            } catch (Exception e) {
                throw new RuntimeException("Lỗi khi lưu file ảnh: " + e.getMessage());
            }
        }

        customerRepository.save(customer);

        return new ResponseObject(Status.SUCCESS, "Cập nhật thông tin thành công", customer);
    }


    @Override
    public ResponseObject changePasswordCustomer(ChangePasswordCustomerRequestDto changePasswordRequestDto,Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Kiểm tra mật khẩu cũ
        if (!passwordEncoder.matches(changePasswordRequestDto.getOldPassword(), customer.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không đúng");
        }

        // Cập nhật mật khẩu mới
        customer.setPassword(passwordEncoder.encode(changePasswordRequestDto.getNewPassword()));
        customerRepository.save(customer);

        return new ResponseObject(Status.SUCCESS, "Đổi mật khẩu thành công", null);
    }
    @Override
    public ResponseObject logout() {
        return new ResponseObject(Status.SUCCESS, "Đăng xuất thành công", null);
    }
}