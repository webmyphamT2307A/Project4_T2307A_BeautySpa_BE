package org.aptech.backendmypham.services.serviceImpl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.CustomerDto;
import org.aptech.backendmypham.models.Customer;
import org.aptech.backendmypham.repositories.CustomerRepository;
import org.aptech.backendmypham.services.CustomerService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final String UPLOAD_DIR = "uploads/";

    @Override
    public List<Customer> getALL() {
        return customerRepository.findAll(); // Chỉ lấy khách hàng đang hoạt động
    }

    @Override
    public Customer findById(Long id) {
        return customerRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void createCustomer(CustomerDto customerDto, MultipartFile file) {
        if (customerDto.getEmail() == null || customerDto.getPhone() == null) {
            throw new RuntimeException("Email và số điện thoại không được để trống!");
        }

        if (customerRepository.findByEmail(customerDto.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã tồn tại!");
        }

        if (customerRepository.findByPhone(customerDto.getPhone()).isPresent()) {
            throw new RuntimeException("Số điện thoại đã tồn tại!");
        }

        Customer customer = new Customer();
        customer.setFullName(customerDto.getFullName());
        customer.setPassword(passwordEncoder.encode(customerDto.getPassword()));
        customer.setEmail(customerDto.getEmail());
        customer.setPhone(customerDto.getPhone());
        customer.setAddress(customerDto.getAddress());
        customer.setIsActive(true);
        customer.setCreatedAt(Instant.now());

        if (file != null && !file.isEmpty()) {
            customer.setImageUrl(saveFile(file));
        }

        customerRepository.save(customer);
    }

    @Override
    @Transactional
    public Customer updateCustomer(Long customerId, CustomerDto customerDto, MultipartFile file) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại!"));

        // Kiểm tra và cập nhật email nếu có thay đổi
        if (customerDto.getEmail() != null && !customerDto.getEmail().equals(customer.getEmail())) {
            customerRepository.findByEmail(customerDto.getEmail()).ifPresent(c -> {
                throw new RuntimeException("Email đã tồn tại!");
            });
            customer.setEmail(customerDto.getEmail());
        }

        // Kiểm tra và cập nhật số điện thoại nếu có thay đổi
        if (customerDto.getPhone() != null && !customerDto.getPhone().equals(customer.getPhone())) {
            customerRepository.findByPhone(customerDto.getPhone()).ifPresent(c -> {
                throw new RuntimeException("Số điện thoại đã tồn tại!");
            });
            customer.setPhone(customerDto.getPhone());
        }

        if (customerDto.getFullName() != null) customer.setFullName(customerDto.getFullName());
        if (customerDto.getAddress() != null) customer.setAddress(customerDto.getAddress());
        if (customerDto.getIsActive() != null) customer.setIsActive(customerDto.getIsActive());

        // Chỉ cập nhật mật khẩu nếu mật khẩu mới được cung cấp và không rỗng
        if (customerDto.getPassword() != null && !customerDto.getPassword().isEmpty()) {
            customer.setPassword(passwordEncoder.encode(customerDto.getPassword()));
        }

        if (file != null && !file.isEmpty()) {
            // Cân nhắc xóa file ảnh cũ nếu cần
            customer.setImageUrl(saveFile(file));
        } else if (customerDto.getImageUrl() != null) {
            customer.setImageUrl(customerDto.getImageUrl());
        }

        return customerRepository.save(customer);
    }

    @Override
    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại!"));
        customer.setIsActive(false);
        customerRepository.save(customer);
    }

    @Override
    public Customer createOrGetGuest(CustomerDto guestDto) {
        // Tìm khách hàng bằng số điện thoại
        Optional<Customer> existingCustomerOpt = customerRepository.findByPhone(guestDto.getPhone());

        if (existingCustomerOpt.isPresent()) {
            // Nếu khách hàng đã tồn tại, hãy kiểm tra xem có cần cập nhật thông tin không
            Customer existingCustomer = existingCustomerOpt.get();
            boolean isUpdated = false;

            // Cập nhật tên nếu tên mới được cung cấp và khác với tên cũ
            if (guestDto.getFullName() != null && !guestDto.getFullName().isBlank() && !guestDto.getFullName().equals(existingCustomer.getFullName())) {
                existingCustomer.setFullName(guestDto.getFullName());
                isUpdated = true;
            }

            // Cập nhật email nếu email mới được cung cấp và khác với email cũ
            if (guestDto.getEmail() != null && !guestDto.getEmail().isBlank() && !guestDto.getEmail().equals(existingCustomer.getEmail())) {
                existingCustomer.setEmail(guestDto.getEmail());
                isUpdated = true;
            }

            // Nếu có bất kỳ thông tin nào được thay đổi, hãy lưu nó vào DB
            if (isUpdated) {
                return customerRepository.save(existingCustomer);
            }

            // Nếu không, chỉ cần trả lại bản ghi hiện có
            return existingCustomer;

        } else {
            // Nếu khách hàng không tồn tại, hãy tạo một khách hàng mới
            Customer newCustomer = new Customer();
            newCustomer.setFullName(guestDto.getFullName());
            newCustomer.setPhone(guestDto.getPhone());
            newCustomer.setEmail(guestDto.getEmail());
            newCustomer.setPassword(passwordEncoder.encode("guest_default_password")); // Giả sử có sẵn passwordEncoder
            newCustomer.setIsActive(true);
            newCustomer.setCreatedAt(Instant.now());

            return customerRepository.save(newCustomer);
        }
    }

    // Hàm private để tái sử dụng logic lưu file
    private String saveFile(MultipartFile file) {
        if (!file.getContentType().startsWith("image/")) {
            throw new RuntimeException("Chỉ cho phép tải lên file ảnh.");
        }

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String timePrefix = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS").format(LocalDateTime.now());
            String fileName = timePrefix + "_" + file.getOriginalFilename().replaceAll("\\s+", "_");
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            return ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/" + UPLOAD_DIR)
                    .path(fileName)
                    .toUriString();
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi lưu file ảnh: " + e.getMessage());
        }
    }
}