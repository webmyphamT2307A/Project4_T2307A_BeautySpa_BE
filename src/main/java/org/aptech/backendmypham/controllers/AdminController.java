package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.ResponseObject;
import org.aptech.backendmypham.dto.UserRequestDto;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.services.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/accounts")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @PostMapping("/create")
    @Operation(summary = "api tạo tài khoản cho admin, nhân viên")
    public ResponseEntity<ResponseObject> createAccount(@RequestBody UserRequestDto userRequestDto) {
        try {
            adminService.createAdmin(


                    userRequestDto.getFullName(),

                    userRequestDto.getPassword(),
                    userRequestDto.getEmail(),
                    userRequestDto.getPhone(),
                    userRequestDto.getAddress(),
                    userRequestDto.getRoleId(),
                    userRequestDto.getBranchId()
            );
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Account created successfully", null)
            );
        } catch (Exception e) {
            return ResponseEntity.ok(
                    new ResponseObject(Status.ERROR, "Lỗi khi tạo tài khoản: " + e.getMessage(), null)
            );
        }
    }

    @PutMapping("/update/{id}")
    @Operation(summary = "api cập nhật thông tin tài khoản admin, nhân viên")
    public ResponseEntity<ResponseObject> updateAccount(@PathVariable Long id, @RequestBody UserRequestDto userRequestDto) {
        try {
            adminService.updateAdmin(
                    id,
                    userRequestDto.getPassword(),
                    userRequestDto.getEmail(),
                    userRequestDto.getPhone(),
                    userRequestDto.getAddress(),
                    userRequestDto.getRoleId(),
                    userRequestDto.getBranchId()
            );
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Account updated successfully", null)
            );
        } catch (Exception e) {
            return ResponseEntity.ok(
                    new ResponseObject(Status.ERROR, "Lỗi khi cập nhật tài khoản: " + e.getMessage(), null)
            );
        }
    }

    @PutMapping("/delete/{id}")
    @Operation(summary = "api xóa tài khoản admin, nhân viên")
    public ResponseEntity<ResponseObject> deleteAccount(@PathVariable Long id) {
        try {
            adminService.deleteAdmin(id);
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Account deleted successfully", null)
            );
        } catch (Exception e) {
            return ResponseEntity.ok(
                    new ResponseObject(Status.ERROR, "Lỗi khi xóa tài khoản: " + e.getMessage(), null)
            );
        }
    }

    @GetMapping("/find-by-id/{id}")
    @Operation(summary = "api tìm tài khoản admin, nhân viên theo id")
    public ResponseEntity<ResponseObject> findById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Tìm tài khoản thành công", adminService.findById(id))
            );
        } catch (Exception e) {
            return ResponseEntity.ok(
                    new ResponseObject(Status.ERROR, "Lỗi khi tìm tài khoản: " + e.getMessage(), null)
            );
        }
    }

    @GetMapping("/find-all")
    @Operation(summary = "api tìm tất cả tài khoản admin, nhân viên")
    public ResponseEntity<ResponseObject> findAll() {
        try {
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Tìm tài khoản thành công", adminService.findAll())
            );
        } catch (Exception e) {
            return ResponseEntity.ok(
                    new ResponseObject(Status.ERROR, "Lỗi khi tìm tài khoản: " + e.getMessage(), null)
            );
        }
    }

    @GetMapping("/find-by-email/{email}")
    @Operation(summary = "api tìm tài khoản admin, nhân viên theo email")
    public ResponseEntity<ResponseObject> findByEmail(@PathVariable String email) {
        try {
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Tìm tài khoản thành công", adminService.findByEmail(email))
            );
        } catch (Exception e) {
            return ResponseEntity.ok(
                    new ResponseObject(Status.ERROR, "Lỗi khi tìm tài khoản: " + e.getMessage(), null)
            );
        }
    }

    @GetMapping("/find-by-phone/{phone}")
    @Operation(summary = "api tìm tài khoản admin, nhân viên theo số điện thoại")
    public ResponseEntity<ResponseObject> findByPhone(@PathVariable String phone) {
        try {
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Tìm tài khoản thành công", adminService.findByPhoneNumber(phone))
            );
        } catch (Exception e) {
            return ResponseEntity.ok(
                    new ResponseObject(Status.ERROR, "Lỗi khi tìm tài khoản: " + e.getMessage(), null)
            );
        }
    }




}

