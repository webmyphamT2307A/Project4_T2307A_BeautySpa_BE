package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.ResponseObject;
import org.aptech.backendmypham.dto.ServiceHistoryDTO;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.services.ServiceHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/serviceHistory")
@RequiredArgsConstructor
public class ServiceHistoryController {
    private  final ServiceHistoryService serviceHistoryService;
    @GetMapping("")
    @Operation(summary = "Lấy tất cả từ service history")
    public ResponseEntity<ResponseObject> getAllServiceHistory() {
        try {
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Tìm  thành công", serviceHistoryService.getAll())
            );
        } catch (Exception e) {
            return ResponseEntity.ok(
                    new ResponseObject(Status.ERROR, "Lỗi khi tìm lich su: " + e.getMessage(), null)
            );
        }
    }
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Lấy lịch sử dịch vụ theo customerId")
    public ResponseEntity<ResponseObject> getServiceHistoryByCustomerId(@PathVariable Integer customerId) {
        try {
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Tìm thành công", serviceHistoryService.getHistoryBycustomerId(customerId))
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ResponseObject(Status.ERROR, "Lỗi khi tìm lịch sử dịch vụ: " + e.getMessage(), null)
            );
        }
    }


}
