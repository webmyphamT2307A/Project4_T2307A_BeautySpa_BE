package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.CustomerDto;
import org.aptech.backendmypham.dto.ResponseObject;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.models.Customer;
import org.aptech.backendmypham.services.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/customers") // Đổi thành "customers" (số nhiều) cho chuẩn RESTful
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    @GetMapping("")
    @Operation(summary = "Lấy tất cả danh sách khách hàng")
    public ResponseEntity<ResponseObject> getAllCustomers() {
        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS, "Lấy danh sách khách hàng thành công.", customerService.getALL())
        );
    }

    // Sử dụng @PathVariable để lấy ID theo chuẩn RESTful
    @GetMapping("/{id}")
    @Operation(summary = "Tìm khách hàng theo ID")
    public ResponseEntity<ResponseObject> getCustomerById(@PathVariable Long id) {
        Customer customer = customerService.findById(id);
        if (customer != null) {
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Tìm thấy khách hàng.", customer)
            );
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ResponseObject(Status.ERROR, "Không tìm thấy khách hàng với ID: " + id, null)
        );
    }

    // Đổi tên endpoint cho ngắn gọn và sử dụng POST trên resource gốc
    @PostMapping(value = "", consumes = {"multipart/form-data"})
    @Operation(summary = "Tạo mới một khách hàng")
    public ResponseEntity<ResponseObject> createCustomer(
            @RequestPart("customer") CustomerDto customerDto,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        try {
            customerService.createCustomer(customerDto, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new ResponseObject(Status.SUCCESS, "Tạo khách hàng thành công.", customerDto)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ResponseObject(Status.ERROR, e.getMessage(), null)
            );
        }
    }

    // Sử dụng @PutMapping cho hành động cập nhật
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    @Operation(summary = "Cập nhật thông tin khách hàng")
    public ResponseEntity<ResponseObject> updateCustomer(
            @PathVariable Long id,
            @RequestPart("customer") CustomerDto customerDto,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        try {
            Customer updatedCustomer = customerService.updateCustomer(id, customerDto, file);
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Cập nhật khách hàng thành công.", updatedCustomer)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ResponseObject(Status.ERROR, e.getMessage(), null)
            );
        }
    }

    // Sử dụng @DeleteMapping cho hành động xóa (mềm)
    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mềm khách hàng (đặt is_active = false)")
    public ResponseEntity<ResponseObject> deleteCustomer(@PathVariable Long id) {
        try {
            customerService.deleteCustomer(id);
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Xóa khách hàng thành công.", null)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ResponseObject(Status.ERROR, e.getMessage(), null)
            );
        }
    }

    @PostMapping("/guest-create")
    @Operation(summary = "Tạo hoặc lấy thông tin khách vãng lai (guest) cho đơn hàng")
    public ResponseEntity<ResponseObject> createOrGetGuestCustomer(@RequestBody CustomerDto guestDto) {
        if (guestDto.getFullName() == null || guestDto.getPhone() == null) {
            return ResponseEntity.badRequest().body(
                    new ResponseObject(Status.ERROR, "Thiếu thông tin tên hoặc số điện thoại.", null)
            );
        }
        try {
            Customer customer = customerService.createOrGetGuest(guestDto);
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Lấy thông tin khách vãng lai thành công.", customer)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ResponseObject(Status.ERROR, e.getMessage(), null)
            );
        }
    }
}