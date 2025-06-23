package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.*;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.models.User;
import org.aptech.backendmypham.services.SalaryService;
import org.aptech.backendmypham.services.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/user/accounts")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final SalaryService salaryService;
    @PostMapping("/create")
    @Operation(summary = "API tạo tài khoản cho role customer")
    public ResponseEntity<ResponseObject> createAccount(@RequestBody UserRequestDto userRequestDto) {
        try {
            if (userRequestDto.getPassword() == null || userRequestDto.getEmail() == null ||
                    userRequestDto.getPhone() == null || userRequestDto.getAddress() == null) {
                throw new RuntimeException("Thông tin không được để trống!");
            }

            if (userRequestDto.getRoleId() == -1) {
                userRequestDto.setRoleId(4);
            }

            userService.createUser(
                    userRequestDto.getFullName(),
                    userRequestDto.getPassword(),
                    userRequestDto.getEmail(),
                    userRequestDto.getPhone(),
                    userRequestDto.getAddress(),
                    userRequestDto.getRoleId()
            );

            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Tạo tài khoản thành công", null)
            );
        } catch (Exception e) {
            return ResponseEntity.ok(
                    new ResponseObject(Status.ERROR, "Lỗi khi tạo tài khoản: " + e.getMessage(), null)
            );
        }
    }
    @PutMapping("/update/{id}")
    @Operation(summary = "Cập nhật lại thông tin tài khoản customer")
    public ResponseEntity<ResponseObject> updateAccount(@PathVariable Long id, @RequestBody UserRequestDto userRequestDto) {
        try {
            userService.updateUser(
                    id,
                    userRequestDto.getPassword(),
                    userRequestDto.getFullName(),
                    userRequestDto.getEmail(),
                    userRequestDto.getPhone(),
                    userRequestDto.getAddress(),
                    userRequestDto.getRoleId()
            );

            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Cập nhật tài khoản thành công", null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject(Status.ERROR, "Lỗi khi cập nhật tài khoản: " + e.getMessage(), null));
        }
    }
    @PutMapping("/delete/{id}")
    @Operation(summary = "Xóa tài khoản khách hàng (role Customer)")
    public ResponseEntity<ResponseObject> deleteCustomerAccount(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Tài khoản khách hàng đã bị vô hiệu hóa!", null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject(Status.ERROR, "Lỗi khi vô hiệu hóa tài khoản khách hàng: " + e.getMessage(), null));
        }
    }
    @GetMapping("/staff")
    public List<User> getStaffUsers() {
        return userService.getUsersByRole("staff");
    }

    @GetMapping("/skill")
    @Operation(summary = "Tìm kiếm kỹ thuật viên theo tiêu chí",
            description = "Tìm kiếm kỹ thuật viên dựa trên kỹ năng, đánh giá, kinh nghiệm. " +
                    "Sử dụng các tham số query để lọc. " +
                    "Sắp xếp có thể dùng: ?sort=averageRating,desc&sort=totalReviews,desc&sort=createdAt,asc")
    public ResponseEntity<Page<TechnicianResponseDTO>> findTechnicians(
            // Spring sẽ tự động map các query params vào các trường của DTO này
            // Ví dụ: ?skillIds=1,2&minAverageRating=4.0
            @ModelAttribute TechnicianSearchCriteriaDTO criteria,
            @PageableDefault(size = 10) Pageable pageable) {
        try {
            Page<TechnicianResponseDTO> technicians = userService.findTechnicians(criteria, pageable);
            return ResponseEntity.ok(technicians);
        } catch (Exception e) {
            // Log lỗi ở đây
            // Trả về lỗi chung chung hơn cho client nếu cần
            // Ví dụ: return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            // Hoặc một Page rỗng với thông báo lỗi (nếu có cấu trúc DTO cho lỗi)
            throw new RuntimeException("Lỗi khi tìm kiếm kỹ thuật viên: " + e.getMessage(), e); // Hoặc xử lý lỗi tốt hơn
        }
    }
    @GetMapping("/salary/estimated")
    @Operation(summary = "Lấy thông tin lương ước tính của nhân viên")
    public SalaryDetails getEstimatedSalary(@RequestParam Long userId) {
        try {
            // Fetch salary details
            var salaryDetails = salaryService.getEstimatedSalary(userId);
            System.out.println("DEBUG: Salary details for userId " + userId + ": " + salaryDetails);
            return salaryDetails;
        } catch (Exception e) {
            // Log the error for debugging/auditing
            System.out.println("Error retrieving salary details: " + e.getMessage());
            return null;
        }
    }
}
