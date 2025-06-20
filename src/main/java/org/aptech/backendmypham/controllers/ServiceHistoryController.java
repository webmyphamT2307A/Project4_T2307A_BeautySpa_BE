package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.GuestServiceHistoryCreateDTO;
import org.aptech.backendmypham.dto.ResponseObject;
import org.aptech.backendmypham.dto.ServiceHistoryDTO;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.services.ServiceHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @PostMapping("/guest")
    @Operation(summary = "Tạo lịch sử dịch vụ cho khách vãng lai")
    public ResponseEntity<ResponseObject> createGuestServiceHistory(@Valid @RequestBody GuestServiceHistoryCreateDTO createDTO) {
        try {
            ServiceHistoryDTO newHistory = serviceHistoryService.createGuestServiceHistory(createDTO);
            return new ResponseEntity<>(
                    new ResponseObject(Status.SUCCESS, "Tạo lịch sử cho khách vãng lai thành công.", newHistory),
                    HttpStatus.CREATED
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ResponseObject(Status.ERROR, e.getMessage(), null)
            );
        }
    }

    @GetMapping("/lookup")
    @Operation(summary = "Tra cứu lịch sử dịch vụ cho khách vãng lai bằng email hoặc SĐT")
    public ResponseEntity<ResponseObject> lookupServiceHistory(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone) {

        if ((email == null || email.trim().isEmpty()) && (phone == null || phone.trim().isEmpty())) {
            return ResponseEntity.badRequest().body(
                    new ResponseObject(Status.ERROR, "Vui lòng cung cấp email hoặc số điện thoại để tra cứu.", null)
            );
        }

        try {
            List<ServiceHistoryDTO> histories = serviceHistoryService.lookupHistory(email, phone);

            if (histories.isEmpty()) {
                return ResponseEntity.ok(new ResponseObject(Status.SUCCESS,
                        "Không tìm thấy lịch sử dịch vụ với thông tin provided.", histories));
            }

            return ResponseEntity.ok(new ResponseObject(Status.SUCCESS,
                    "Tìm thấy " + histories.size() + " lịch sử dịch vụ.", histories));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ResponseObject(Status.ERROR, "Lỗi hệ thống khi tra cứu lịch sử: " + e.getMessage(), null)
            );
        }
    }


}
