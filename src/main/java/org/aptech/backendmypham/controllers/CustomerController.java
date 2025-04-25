package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.*;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.services.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customer/")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    @PostMapping("/login")
    @Operation(summary = "đăng nhập cho khách hàng")
    public ResponseEntity<ResponseObject> loginCustomer(@RequestBody LoginCustomerDto Cdto) {
        return ResponseEntity.ok(customerService.loginCustomer(Cdto));
    }
    @PostMapping("/register")
    @Operation(summary = "đăng ký cho khách hàng")
    public ResponseEntity<ResponseObject> registerCustomer(@RequestBody RegisterRequestDto registerRequestDto) {
        try {
            ResponseObject user = customerService.registerCustomer(registerRequestDto);
            return ResponseEntity.ok(new ResponseObject(Status.SUCCESS, "Đăng ký thành công", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseObject(Status.ERROR, e.getMessage(), null));
        }
    }
    @GetMapping("/detail/{id}")
    @Operation(summary = "Thông tin chi tiết về khách hàng")
    public ResponseEntity<ResponseObject> detail(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerDetail(id));
    }

    @PutMapping("/update-info/{id}")
    @Operation(summary = "Cập nhập thông tin cho khách hàng")
    public ResponseEntity<ResponseObject> updateInfo(@PathVariable Long id, @RequestBody CustomerDetailResponseDto Cdto) {
        return ResponseEntity.ok(customerService.updateCustomer(id,Cdto));
    }

    @PutMapping("/change-password/{id}")
    public ResponseEntity<ResponseObject> changePassword(@PathVariable Long id, @RequestBody ChangePasswordCustomerRequestDto dto) {
        return ResponseEntity.ok(customerService.changePasswordCustomer(dto,id));
    }



    @PostMapping("/logout")
    public ResponseEntity<ResponseObject> logout() {
        return ResponseEntity.ok(customerService.logout());
    }
}
