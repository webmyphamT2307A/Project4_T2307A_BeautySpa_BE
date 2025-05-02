package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.CustomerDto;
import org.aptech.backendmypham.dto.ResponseObject;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.services.CustomerService;
import org.aptech.backendmypham.services.serviceImpl.CustomerServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
public class CustomerController {
    private  final CustomerService customerService;

    @GetMapping("")
    @Operation(summary = "Lấy tất cả của customer")
    public ResponseEntity<ResponseObject> getAllCustomer(){
        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS,"Lấy thành công",customerService.getALL())
        );
    }
    @GetMapping("/findById")
    @Operation(summary = "Lấy id của customer")
    public ResponseEntity<ResponseObject> getCustomerById(Long Uid){
        try {
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Tìm tài khoản thành công", customerService.findById(Uid))
            );
        } catch (Exception e) {
            return ResponseEntity.ok(
                    new ResponseObject(Status.ERROR, "Lỗi khi tìm tài khoản: " + e.getMessage(), null)
            );
        }

    }

    @PostMapping("/created")
    @Operation(summary = "Tạo customer")
    public ResponseEntity<ResponseObject> createdCustomer(
            @RequestPart("customer") CustomerDto customerDto,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        try {
            customerService.createCustomer(
                    customerDto.getPassword(),
                    customerDto.getFullName(),
                    customerDto.getEmail(),
                    customerDto.getPhone(),
                    customerDto.getAddress(),
                    file
            );
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Customer created successfully", null)
            );
        } catch (Exception e) {
            return ResponseEntity.ok(
                    new ResponseObject(Status.ERROR, "Lỗi khi tạo tài khoản: " + e.getMessage(), null)
            );
        }
    }

    @PostMapping(value = "/update/{id}", consumes = {"multipart/form-data"})
    @Operation(summary = "api cập nhật thông tin tài khoản khách hàng")
    public ResponseEntity<ResponseObject> updateCustomer(
            @PathVariable Long id,
            @RequestPart("customer") CustomerDto customerDto,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        try {
            customerService.updateCustomer(
                    id,
                    customerDto.getPassword(),
                    customerDto.getFullName(),
                    customerDto.getEmail(),
                    customerDto.getPhone(),
                    customerDto.getAddress(),
                    customerDto.getIsActive(),
                    customerDto.getImageUrl(),
                    file
            );
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Customer updated successfully", null)
            );
        } catch (Exception e) {
            return ResponseEntity.ok(
                    new ResponseObject(Status.ERROR, "Lỗi khi cập nhật tài khoản: " + e.getMessage(), null)
            );
        }
    }

    @PutMapping("/delete/{id}")
    @Operation(summary = "Xóa mềm customer")
    public  ResponseEntity<ResponseObject> deleteCustomers(@PathVariable Long id){
        try {
            customerService.deleteCustomer(id);
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Customer deleted successfully", null)
            );
        } catch (Exception e) {
            return ResponseEntity.ok(
                    new ResponseObject(Status.ERROR, "Lỗi khi xóa tài khoản: " + e.getMessage(), null)
            );
        }
    }
}