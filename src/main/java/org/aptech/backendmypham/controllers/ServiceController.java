package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.ServiceRequestDto;
import org.aptech.backendmypham.dto.ServiceResponseDto;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.exception.ResourceNotFoundException;
import org.aptech.backendmypham.models.Service;
import org.aptech.backendmypham.services.ServicesService;
import org.aptech.backendmypham.dto.ResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/services")
public class ServiceController {

    private final ServicesService servicesService;

    @GetMapping("")
    @Operation(summary = "Lấy tất cả service trong hệ thống")
    public ResponseEntity<ResponseObject> getAllService() {
        List<ServiceResponseDto> serviceDtos = servicesService.getAllService();
        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS, "Lấy danh sách dịch vụ thành công", serviceDtos)
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Tìm dịch vụ theo ID")
    public ResponseEntity<ResponseObject> findServiceById(@PathVariable Integer id) {
        return servicesService.findById(id)
                .map(serviceDto -> ResponseEntity.ok(
                        new ResponseObject(Status.SUCCESS, "Tìm thấy dịch vụ", serviceDto)
                ))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new ResponseObject(Status.FAIL, "Không tìm thấy dịch vụ với ID = " + id, null)
                ));
    }


    @GetMapping("/find-by-name")
    @Operation(summary = "Tìm dịch vụ theo tên")
    public ResponseEntity<ResponseObject> findByName(@RequestParam String name) {
        return servicesService.findByName(name)
                .map(serviceDto -> ResponseEntity.ok(new ResponseObject(Status.SUCCESS, "Tìm thấy dịch vụ", serviceDto)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new ResponseObject(Status.FAIL, "Không tìm thấy dịch vụ với tên = " + name, null)));
    }
    @PostMapping("")
    @Operation(summary = "Tạo một dịch vụ mới")
    public ResponseEntity<ResponseObject> createService(@RequestBody ServiceRequestDto serviceRequestDto) {
        // Gọi service với DTO request
        ServiceResponseDto createdServiceDto = servicesService.createService(serviceRequestDto);
        // Trả về mã 201 Created là chuẩn cho việc tạo mới thành công
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject(Status.SUCCESS, "Tạo dịch vụ thành công", createdServiceDto)
        );
    }
    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thông tin dịch vụ")
    public ResponseEntity<ResponseObject> updateService(
            @PathVariable Integer id,
            @RequestBody ServiceRequestDto serviceRequestDto // Sửa: Nhận vào DTO
    ) {
        try {
            // Gọi service với id và DTO request
            ServiceResponseDto updatedDto = servicesService.updateService(id, serviceRequestDto);
            return ResponseEntity.ok(new ResponseObject(Status.SUCCESS, "Cập nhật thành công", updatedDto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ResponseObject(Status.FAIL, e.getMessage(), null));
        }
    }

    @PutMapping("/delete/{id}")
    @Operation(summary = "Xóa service")
    public ResponseEntity<ResponseObject> softDeleteService(@PathVariable Integer id) {
        try {
            servicesService.softDeleteService(id);
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Dịch vụ đã được xóa mềm", null)
            );
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseObject(Status.FAIL, e.getMessage(), null));
        }
    }
    @GetMapping("/monthly-history")
    @Operation(summary = "Lấy lịch sử đơn hàng theo tháng của một user cụ thể")
    public ResponseEntity<ResponseObject> getMonthlyHistory(
            @RequestParam Long userId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        try {
            Map<String, List<Object>> data = servicesService.getMonthlyHistory(userId, year, month);
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Lấy lịch sử đơn hàng thành công", data)
            );
        } catch (Exception e) {
            System.out.println("Error getting monthly history: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ResponseObject(Status.ERROR, "Lỗi khi lấy lịch sử đơn hàng: " + e.getMessage(), null)
            );
        }
    }
}