package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.models.Service;
import org.aptech.backendmypham.services.ServiceService;
import org.aptech.backendmypham.dto.ResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/services")
public class ServiceController {

    @Autowired
    private ServiceService serviceService;

   @GetMapping("")
    @Operation(summary = "Lấy tất cả service trong hệ thống")
    public ResponseEntity<ResponseObject> getAllService() {
        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS, "Thành công", servicesService.gellALlService())
        );

    }
  
    @GetMapping("/{id}")
    @Operation(summary = "Tìm dịch vụ theo ID")
    public ResponseEntity<ResponseObject> findServiceById(@PathVariable Integer id) {
        return serviceService.findById(id)
                .map(service -> ResponseEntity.ok(
                        new ResponseObject(Status.SUCCESS, "Tìm thấy dịch vụ", service)
                ))
                .orElseGet(() -> ResponseEntity.status(404).body(
                        new ResponseObject(Status.FAIL, "Không tìm thấy dịch vụ với ID = " + id, null)
                ));
    }

    @GetMapping("/find-by-name")
    @Operation(summary = "Tìm dịch vụ theo tên")
    public ResponseEntity<ResponseObject> findByName(@RequestParam String name) {
        return serviceService.findByName(name)
                .map(service -> ResponseEntity.ok(new ResponseObject(Status.SUCCESS, "Tìm thấy dịch vụ", service)))
                .orElseGet(() -> ResponseEntity.ok(new ResponseObject(Status.FAIL, "Không tìm thấy dịch vụ", null)));
    }
    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thông tin dịch vụ")
    public ResponseEntity<ResponseObject> updateService(
            @PathVariable Integer id,
            @RequestBody Service updatedService
    ) {
        try {
            Service updated = serviceService.updateService(id, updatedService);
            return ResponseEntity.ok(new ResponseObject(Status.SUCCESS, "Cập nhật thành công", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(new ResponseObject(Status.FAIL, e.getMessage(), null));
        }
    }
    @PutMapping("/delete/{id}")
    @Operation(summary = "Xóa service")
    public ResponseEntity<ResponseObject> softDeleteService(@PathVariable Integer id) {
        try {
            serviceService.softDeleteService(id);
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Service đã bị vô hiệu hóa", null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject(Status.FAIL, "Lỗi khi xóa service: " + e.getMessage(), null));
        }

   

