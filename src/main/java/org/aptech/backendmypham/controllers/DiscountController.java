package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import org.aptech.backendmypham.models.Discount;
import org.aptech.backendmypham.services.DiscountService;
import org.aptech.backendmypham.dto.ResponseObject;
import org.aptech.backendmypham.enums.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/api/discounts")
public class DiscountController {

    @Autowired
    private DiscountService discountService;

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả các mã giảm giá")
    public ResponseEntity<ResponseObject> getAllDiscounts() {
        List<Discount> discounts = discountService.getAllDiscounts();
        return ResponseEntity.ok(new ResponseObject(Status.SUCCESS, "Lấy danh sách thành công", discounts));
    }
}