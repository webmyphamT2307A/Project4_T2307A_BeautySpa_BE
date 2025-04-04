package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import org.aptech.backendmypham.models.Discount;
import org.aptech.backendmypham.services.DiscountService;
import org.aptech.backendmypham.dto.ResponseObject;
import org.aptech.backendmypham.enums.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/discounts")
public class DiscountController {

    @Autowired
    private DiscountService discountService;

    @PostMapping("/create")
    @Operation(summary = "Tạo mới mã giảm giá")
    public ResponseEntity<ResponseObject> createDiscount(@RequestBody Discount discount) {
        Discount newDiscount = discountService.createDiscount(discount);
        return ResponseEntity.ok(new ResponseObject(Status.SUCCESS, "Tạo mã giảm giá thành công", newDiscount));
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả các mã giảm giá")
    public ResponseEntity<ResponseObject> getAllDiscounts() {
        List<Discount> discounts = discountService.getAllDiscounts();
        return ResponseEntity.ok(new ResponseObject(Status.SUCCESS, "Lấy danh sách thành công", discounts));
    }

    @PutMapping("/update")
    @Operation(summary = "Cập nhật thông tin mã giảm giá")
    public ResponseEntity<ResponseObject> updateDiscount(@PathVariable Integer id, @RequestBody Discount discount) {
        try {
            Discount updatedDiscount = discountService.updateDiscount(id, discount);
            return ResponseEntity.ok(new ResponseObject(Status.SUCCESS, "Cập nhật thành công", updatedDiscount));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(new ResponseObject(Status.FAIL, e.getMessage(), null));
        }
    }

    @GetMapping("/findById")
    @Operation(summary = "Tìm kiếm mã giảm giá theo ID")
    public ResponseEntity<ResponseObject> getDiscountById(@PathVariable Integer id) {
        Optional<Discount> discount = discountService.findDiscountById(id);

        if (discount.isPresent()) {
            return ResponseEntity.ok(new ResponseObject(Status.SUCCESS, "Tìm thấy mã giảm giá", discount.get()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseObject(Status.FAIL, "Không tìm thấy mã giảm giá", null));
        }
    }
    @PutMapping("/delete/{id}")
    @Operation(summary = "Xóa mềm discount bằng cách vô hiệu hóa")
    public ResponseEntity<ResponseObject> softDeleteDiscount(@PathVariable Integer id) {
        if (discountService.getDiscountById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseObject(Status.FAIL, "Discount không tồn tại hoặc đã bị vô hiệu hóa", null));
        }

        discountService.softDeleteDiscount(id);
        return ResponseEntity.ok(new ResponseObject(Status.SUCCESS, "Discount đã bị vô hiệu hóa", null));
    }

}