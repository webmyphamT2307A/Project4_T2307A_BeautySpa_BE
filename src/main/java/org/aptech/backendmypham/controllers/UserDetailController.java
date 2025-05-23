package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.configs.JwtService;
import org.aptech.backendmypham.dto.*;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.models.User;
import org.aptech.backendmypham.services.userDetailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/userDetail")
@RequiredArgsConstructor
public class UserDetailController {
    private final JwtService jwtService;
    private final userDetailService userDetailService;

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập cho user (nhân viên)")
    public ResponseEntity<ResponseObject> login(@RequestBody LoginRequestDto dto) {
        return ResponseEntity.ok(userDetailService.login(dto));
    }

    @PostMapping("/register")
    @Operation(summary = "Đăng ký cho user (nhân viên)")
    public ResponseEntity<ResponseObject> register(@RequestBody UserRegisterDto userRegisterDto) {
        try {
            ResponseObject user = userDetailService.registerUser(userRegisterDto);
            return ResponseEntity.ok(new ResponseObject(Status.SUCCESS, "Đăng ký thành công", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseObject(Status.ERROR, e.getMessage(), null));
        }
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "Thông tin chi tiết về user (nhân viên)")
    public ResponseEntity<ResponseObject> detail(@PathVariable Long id) {
        return ResponseEntity.ok(userDetailService.getUserDetail(id));
    }

    @PutMapping("/update-info/{id}")
    @Operation(summary = "Cập nhật thông tin cho user (nhân viên)")
    public ResponseEntity<ResponseObject> updateInfo(@PathVariable Long id, @RequestBody UserInfoUpdateDto dto) {
        return ResponseEntity.ok(userDetailService.updateInfo(id, dto));
    }

    @PutMapping("/change-password/{id}")
    public ResponseEntity<ResponseObject> changePassword(@PathVariable Long id, @RequestBody UserPasswordChangeDto dto) {
        return ResponseEntity.ok(userDetailService.changePassword(id, dto));
    }
    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin người dùng hiện tại từ token")
    public ResponseEntity<ResponseObject> getCurrentUser(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            String email = jwtService.getSubjectFromToken(token);
            User user = userDetailService.getUserByEmail(email);
            return ResponseEntity.ok(new ResponseObject(Status.SUCCESS, "Lấy thông tin người dùng thành công", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseObject(Status.ERROR, e.getMessage(), null));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseObject> logout() {
        return ResponseEntity.ok(userDetailService.logout());
    }
}
