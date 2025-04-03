package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.ResponseObject;
import org.aptech.backendmypham.dto.UserRequestDto;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user/accounts")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/create")
    @Operation(summary = "API tạo tài khoản cho role customer")
    public ResponseEntity<ResponseObject> createAccount(@RequestBody UserRequestDto userRequestDto) {
        try {
            // Validate the request data
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

}
