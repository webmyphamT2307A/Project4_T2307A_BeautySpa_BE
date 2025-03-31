package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.ResponseObject;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.models.Product;
import org.aptech.backendmypham.services.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1/api/product")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("")
    @Operation(summary = "Lấy hết ở product")
    public  ResponseEntity<ResponseObject> findAllProduct(){
        return  ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS,"Thành công",productService.getALlProduct())
        );
    }

    @GetMapping("/findById")
    @Operation(summary = "Lấy theo id Product")
    public  ResponseEntity<ResponseObject> findById(@RequestParam Long Pid){
        Product product = productService.findById(Pid);
        if(product != null){
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Thành công", product)
            );
        } else {
            return ResponseEntity.ok(
                    new ResponseObject(Status.ERROR, "Không tìm thấy product", null)
            );
        }
    }


    @PostMapping("/create")
    @Operation(summary = "Tạo mới product")
    public ResponseEntity<ResponseObject> createProduct(@RequestBody Product product) {
        try {
            Product savedProduct = productService.createProduct(product);
            return ResponseEntity.ok(new ResponseObject(Status.SUCCESS, "Tạo sản phẩm thành công", savedProduct));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseObject(Status.ERROR, "Lỗi khi tạo sản phẩm", null));
        }
    }
    @PutMapping("/update")
    @Operation(summary = "update lại product")
    public ResponseEntity<ResponseObject> updateProduct(@RequestParam Long PiD,Product updateProduct) {
        try{
            productService.updateProduct(PiD, updateProduct);
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Cập nhật product thành công", null)
            );
        } catch (Exception e) {
            return ResponseEntity.ok(
                    new ResponseObject(Status.ERROR, "Cập nhật product thất bại: " + e.getMessage(), null)
            );
        }
    }
    @PutMapping("/delete")
    @Operation(summary = "xóa product")
    public ResponseEntity<ResponseObject> deleteProduct(@RequestParam Long Pid) {
        try{
            productService.deleteProduct(Pid);
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Xóa product thành công", null)
            );
        } catch (Exception e) {
            return ResponseEntity.ok(
                    new ResponseObject(Status.ERROR, "Xóa product thất bại: " + e.getMessage(), null)
            );
        }
    }
}
