package org.aptech.backendmypham.services.serviceImpl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.models.Branch;
import org.aptech.backendmypham.models.Customer;
import org.aptech.backendmypham.models.Role;
import org.aptech.backendmypham.models.User;
import org.aptech.backendmypham.repositories.CustomerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements org.aptech.backendmypham.services.CustomerService {
    private  final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<Customer> getALL(){
        return customerRepository.findAll();
    }
    @Override
    public Customer findById(Long UiD){
        return customerRepository.findById(UiD).orElse(null);
    }
    @Override
    public void createCustomer(String password, String fullName, String email, String phoneNumber, String address, MultipartFile file) {
        if (password == null || email == null || phoneNumber == null || address == null) {
            throw new RuntimeException("Thông tin không được để trống!");
        }

        // Bất đồng bộ kiểm tra sự tồn tại của email và phone
        CompletableFuture<Optional<Customer>> emailFuture = CompletableFuture.supplyAsync(() -> customerRepository.findByEmail(email));
        CompletableFuture<Optional<Customer>> phoneFuture = CompletableFuture.supplyAsync(() -> customerRepository.findByPhone(phoneNumber));

        try {
            Optional<Customer> emailOpt = emailFuture.get(5, TimeUnit.SECONDS);
            Optional<Customer> phoneOpt = phoneFuture.get(5, TimeUnit.SECONDS);

            if (emailOpt.isPresent()) {
                throw new RuntimeException("Email đã tồn tại!");
            }

            if (phoneOpt.isPresent()) {
                throw new RuntimeException("Số điện thoại đã tồn tại!");
            }

            // Tạo mới Customer
            Customer customer = new Customer();
            customer.setFullName(fullName);
            customer.setPassword(passwordEncoder.encode(password));
            customer.setEmail(email);
            customer.setPhone(phoneNumber);
            customer.setAddress(address);
            customer.setIsActive(true);
            customer.setCreatedAt(Instant.now());
            if (file != null && !file.isEmpty()) {
                try {
                    String uploadDir = "uploads/";
                    java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);
                    if (!java.nio.file.Files.exists(uploadPath)) {
                        java.nio.file.Files.createDirectories(uploadPath);
                    }

                    String fileType = file.getContentType();
                    if (!fileType.startsWith("image/")) {
                        throw new RuntimeException("Chỉ cho phép tải lên ảnh.");
                    }

                    String timePrefix = java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")
                            .format(java.time.LocalDateTime.now());
                    String fileName = timePrefix + "_" + file.getOriginalFilename().replaceAll("\\s+", "_");
                    java.nio.file.Path filePath = uploadPath.resolve(fileName);
                    java.nio.file.Files.copy(file.getInputStream(), filePath);

                    String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
                    String imageUrls = baseUrl + "/uploads/" + fileName;
                    customer.setImageUrl(imageUrls);
                } catch (Exception e) {
                    throw new RuntimeException("Lỗi khi lưu file ảnh: " + e.getMessage());
                }
            }

            customerRepository.save(customer);


        } catch (TimeoutException e) {
            throw new RuntimeException("Một trong các yêu cầu kiểm tra dữ liệu mất quá nhiều thời gian. Vui lòng thử lại sau.");
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Lỗi khi kiểm tra tồn tại email hoặc số điện thoại: " + e.getMessage());
        }
    }
    @Override
    @Transactional
    public void updateCustomer(Long CustomerId, String password, String fullName, String email, String phoneNumber, String address, Boolean isActive, String imageUrl, MultipartFile file) {
        Optional<Customer> customerOpt = customerRepository.findById(CustomerId);
        if (customerOpt.isEmpty()) {
            throw new RuntimeException("Người dùng không tồn tại!");
        }
        Customer customer = customerOpt.get();

        if (fullName != null) customer.setFullName(fullName);
        if (password != null) customer.setPassword(passwordEncoder.encode(password));
        if (email != null && !email.equals(customer.getEmail())) {
            Optional<Customer> emailOpt = customerRepository.findByEmail(email);
            if (emailOpt.isPresent()) throw new RuntimeException("Email đã tồn tại!");
            customer.setEmail(email);
        }
        if (phoneNumber != null && !phoneNumber.equals(customer.getPhone())) {
            Optional<Customer> phoneOpt = customerRepository.findByPhone(phoneNumber);
            if (phoneOpt.isPresent()) throw new RuntimeException("Số điện thoại đã tồn tại!");
            customer.setPhone(phoneNumber);
        }
        if (address != null) customer.setAddress(address);
        if (isActive != null) customer.setIsActive(isActive);

        // Nếu có file thì lưu avatar mới
        if (file != null && !file.isEmpty()) {
            try {
                String uploadDir = "uploads/";
                java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);
                if (!java.nio.file.Files.exists(uploadPath)) {
                    java.nio.file.Files.createDirectories(uploadPath);
                }
                String fileType = file.getContentType();
                if (!fileType.startsWith("image/")) {
                    throw new RuntimeException("Chỉ cho phép tải lên ảnh.");
                }
                String timePrefix = java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")
                        .format(java.time.LocalDateTime.now());
                String fileName = timePrefix + "_" + file.getOriginalFilename().replaceAll("\\s+", "_");
                java.nio.file.Path filePath = uploadPath.resolve(fileName);
                java.nio.file.Files.copy(file.getInputStream(), filePath);

                String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
                String imageUrls = baseUrl + "/uploads/" + fileName;
                customer.setImageUrl(imageUrls);
            } catch (Exception e) {
                throw new RuntimeException("Lỗi khi lưu file ảnh: " + e.getMessage());
            }
        } else if (imageUrl != null && !imageUrl.isEmpty()) {
            // Nếu không upload file mới, giữ lại ảnh cũ từ FE gửi lên
            customer.setImageUrl(imageUrl);
        }

        customerRepository.save(customer);
    }
    @Override
    @Transactional
    public  void deleteCustomer(Long Cid){
        Customer customer = customerRepository.findById(Cid).orElse(null);
        if (customer == null) {
            throw new RuntimeException("Người dùng không tồn tại!");
        }


        customer.setIsActive(false);
        customerRepository.save(customer);
    }
}
