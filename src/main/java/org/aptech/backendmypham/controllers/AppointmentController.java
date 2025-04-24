package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.AppointmentDto;
import org.aptech.backendmypham.dto.AppointmentResponseDto;
import org.aptech.backendmypham.dto.ResponseObject;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.models.Appointment;
import org.aptech.backendmypham.models.Product;
import org.aptech.backendmypham.services.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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


}
