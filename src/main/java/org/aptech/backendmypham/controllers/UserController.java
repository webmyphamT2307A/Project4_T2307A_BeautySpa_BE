package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.ResponseObject;
import org.aptech.backendmypham.dto.UserRequestDto;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/accounts")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/create")
    @Operation(summary = "api tạo tài khoản cho admin, nhân viên")
    public ResponseEntity<ResponseObject> createAccount(@RequestBody UserRequestDto userRequestDto) {
        try {
            userService.createUser(
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
}
