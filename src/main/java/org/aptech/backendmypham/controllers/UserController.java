package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.ResponseObject;
import org.aptech.backendmypham.dto.UserRequestDto;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.models.User;
import org.aptech.backendmypham.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/user/accounts")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

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
                    userRequestDto.getRoleId(),
                    userRequestDto.getBranchId()
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
                    userRequestDto.getRoleId(),
                    userRequestDto.getBranchId()
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

}
