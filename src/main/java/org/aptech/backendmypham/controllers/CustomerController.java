package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.CustomerDto;
import org.aptech.backendmypham.dto.ResponseObject;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.services.serviceImpl.CustomerServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
public class CustomerController {
    private  final CustomerServiceImpl customerService;

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
    public  ResponseEntity<ResponseObject> createdCustomer(@RequestBody CustomerDto customerDto){
        try {
            customerService.createCustomer(
                    customerDto.getFullName(),

                    customerDto.getPassword(),
                    customerDto.getEmail(),
                    customerDto.getPhone(),
                    customerDto.getAddress(),
                    customerDto.getImageUrl()

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
    @PutMapping("/update/{id}")
    @Operation(summary = "api cập nhật thông tin tài khoản khách hàng")
    public ResponseEntity<ResponseObject> updateCustomer(@PathVariable Long Cid, @RequestBody CustomerDto customerDto){
        try {
            customerService.updateCustomer(
                    Cid,
                    customerDto.getFullName(),
                    customerDto.getPassword(),
                    customerDto.getEmail(),
                    customerDto.getPhone(),
                    customerDto.getAddress(),
                    customerDto.getImageUrl(),
                    customerDto.getIsActive()

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
    public  ResponseEntity<ResponseObject> deleteCustomers(@PathVariable Long Cid){
        try {
            customerService.deleteCustomer(Cid);
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
