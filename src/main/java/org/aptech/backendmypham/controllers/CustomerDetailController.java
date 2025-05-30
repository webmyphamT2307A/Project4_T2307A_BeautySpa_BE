package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.*;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.services.CustomerDetailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/customer/")
@RequiredArgsConstructor
public class CustomerDetailController {
    private final CustomerDetailService customerDetailService;

    @PostMapping("/login")
    @Operation(summary = "đăng nhập cho khách hàng")
    public ResponseEntity<ResponseObject> loginCustomer(@RequestBody LoginCustomerDto Cdto) {
        return ResponseEntity.ok(customerDetailService.loginCustomer(Cdto));
    }
    @PostMapping("/register")
    @Operation(summary = "đăng ký cho khách hàng")
    public ResponseEntity<ResponseObject> registerCustomer(@RequestBody RegisterRequestDto registerRequestDto) {
        try {
            ResponseObject user = customerDetailService.registerCustomer(registerRequestDto);
            return ResponseEntity.ok(new ResponseObject(Status.SUCCESS, "Đăng ký thành công", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseObject(Status.ERROR, e.getMessage(), null));
        }
    }
    @GetMapping("/detail/{id}")
    @Operation(summary = "Thông tin chi tiết về khách hàng")
    public ResponseEntity<ResponseObject> detail(@PathVariable Long id) {
        return ResponseEntity.ok(customerDetailService.getCustomerDetail(id));
    }

    @PutMapping(value = "/update-info/{id}", consumes = {"multipart/form-data"})
    @Operation(summary = "Cập nhật thông tin cho khách hàng")
    public ResponseEntity<ResponseObject> updateInfo(
            @PathVariable Long id,
            @RequestPart("info") CustomerDetailResponseDto Cdto,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.ok(customerDetailService.updateCustomer(id, Cdto, file));
    }


    @PutMapping("/change-password/{id}")
    public ResponseEntity<ResponseObject> changePassword(@PathVariable Long id, @RequestBody ChangePasswordCustomerRequestDto dto) {
        return ResponseEntity.ok(customerDetailService.changePasswordCustomer(dto,id));
    }



    @PostMapping("/logout")
    public ResponseEntity<ResponseObject> logout() {
        return ResponseEntity.ok(customerDetailService.logout());
    }
}