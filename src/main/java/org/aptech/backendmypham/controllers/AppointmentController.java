package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.*;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.services.AppointmentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/appointment")
@RequiredArgsConstructor
public class AppointmentController {
    private final AppointmentService appointmentService;

    @PostMapping("/create")
    @Operation(summary = "tạo mới cho appointment")

    public ResponseEntity<ResponseObject> createdAppointment(@RequestBody  AppointmentDto dto) {
        try {
            appointmentService.createAppointment(dto);
            return ResponseEntity.ok(new ResponseObject(Status.SUCCESS, "Tạo Appointment thành công", null));
        } catch (Exception e) {
            e.printStackTrace(); // ✅ Thêm dòng này để hiện lỗi trong terminal/log
            return ResponseEntity.badRequest()
                    .body(new ResponseObject(Status.ERROR, "Lỗi khi tạo Appointment: " + e.getMessage(), null));
        }
    }

    @GetMapping("/findById")
    @Operation(summary = "Lấy id của appointment")
     public ResponseEntity<ResponseObject> findById(@RequestParam Long AiD) {
        AppointmentResponseDto appointment = appointmentService.findByIdAndIsActive(AiD);
        if (appointment != null) {
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Thành công", appointment)
            );
        } else {
            return ResponseEntity.ok(
                    new ResponseObject(Status.ERROR, "Không tìm thấy appointment", null)
            );
        }
    }
    @PutMapping("/{id}/cancel")
    @Operation(summary = "Hủy một lịch hẹn theo ID")
    public ResponseEntity<ResponseObject> cancelAppointment(@PathVariable("id") Long id) {
        try {
            appointmentService.cancelAppointment(id);
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Hủy lịch hẹn thành công", null)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ResponseObject(Status.ERROR, "Lỗi khi hủy lịch hẹn: " + e.getMessage(), null)
            );
        }
    }
    @PutMapping("/update")
    @Operation(summary = "Cập nhật thông tin appointment theo ID")
    public ResponseEntity<ResponseObject> updateAppointment(@RequestParam Long AiD, @RequestBody AppointmentDto dto) {
        try {
            appointmentService.updateAppointment(AiD, dto);
            return ResponseEntity.ok(new ResponseObject(Status.SUCCESS, "Cập nhật thành công", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseObject(Status.ERROR, e.getMessage(), null));
        }
    }
    @PutMapping("/delete")
    @Operation(summary = "Xóa mềm appointment theo ID ")
    public ResponseEntity<ResponseObject> deleteAppointment(@RequestParam Long AiD) {
        try {
            appointmentService.deleteAppointment(AiD);
            return ResponseEntity.ok(new ResponseObject(Status.SUCCESS, "Xóa mềm thành công", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseObject(Status.ERROR, e.getMessage(), null));
        }
    }
    @GetMapping("")
    @Operation(summary = "Lấy tất cả appointment")
    public  ResponseEntity<ResponseObject> getALlAppointment() {
        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS,"Lấy thành công",appointmentService.getALlAppointment())
        );
    }
    @GetMapping("/byUser")
    @Operation(summary = "Lấy danh sách lịch hẹn theo userId")
    public ResponseEntity<ResponseObject> getAppointmentsByUserId(@RequestParam Long userId) {
        try {
            List<AppointmentResponseDto> appointments = appointmentService.getAppointmentsByUserId(userId);
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Lấy danh sách lịch hẹn thành công", appointments)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ResponseObject(Status.ERROR, "Lỗi khi lấy danh sách lịch hẹn: " + e.getMessage(), null)
            );
        }
    }
    @GetMapping("/today")
    public Map<String, Object> getTodayServices(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "userId", required = false) Long userId
        ) {
        if (date == null) {
            date = LocalDate.now();
        }
        Map<String, Object> response = appointmentService.getAppointmentsGroupedByShift(date, userId);
        System.out.println("Response: " + response);
        return response;
    }
    // ✅ NEW ENDPOINTS FOR SERVICE HISTORY
    @GetMapping("/history/customer/{customerId}")
    @Operation(summary = "Lấy lịch sử appointment theo customer ID")
    public ResponseEntity<ResponseObject> getCustomerAppointmentHistory(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            List<AppointmentHistoryDTO> history = appointmentService.getCustomerAppointmentHistory(customerId, page, size);
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Lấy lịch sử appointment thành công", history)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ResponseObject(Status.ERROR, "Lỗi: " + e.getMessage(), null)
            );
        }
    }

    @GetMapping("/history/phone/{phoneNumber}")
    @Operation(summary = "Tra cứu lịch sử appointment theo số điện thoại")
    public ResponseEntity<ResponseObject> getAppointmentHistoryByPhone(
            @PathVariable String phoneNumber
    ) {
        try {
            List<AppointmentHistoryDTO> history = appointmentService.getAppointmentHistoryByPhone(phoneNumber);
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Tra cứu appointment thành công", history)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ResponseObject(Status.ERROR, "Không tìm thấy appointment với số điện thoại này", null)
            );
        }
    }

    @GetMapping("/stats/customer/{customerId}")
    @Operation(summary = "Lấy thống kê appointment theo customer ID")
    public ResponseEntity<ResponseObject> getCustomerAppointmentStats(@PathVariable Long customerId) {
        try {
            AppointmentStatsDTO stats = appointmentService.getCustomerAppointmentStats(customerId);
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Lấy thống kê appointment thành công", stats)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ResponseObject(Status.ERROR, "Lỗi: " + e.getMessage(), null)
            );
        }
    }

    @PutMapping("/{serviceId}/complete")
    public void markServiceAsComplete(@PathVariable Long serviceId) {
        appointmentService.markServiceAsComplete(serviceId);
    }

}
