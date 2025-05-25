package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.AttendanceCheckInOutDTO;
import org.aptech.backendmypham.dto.ResponseObject;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.models.Attendance;
import org.aptech.backendmypham.models.User;
import org.aptech.backendmypham.repositories.UserRepository;
import org.aptech.backendmypham.services.AttendanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/attendance")
public class AttendanceController {
    final private AttendanceService attendanceService;
    private final UserRepository userRepository;
    @GetMapping("/find-all")
    @Operation(summary = "api tìm tất cả lịch điểm danh")
    public ResponseEntity<ResponseObject> findAll() {
        try {
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Tìm lịch điểm danh thành công", attendanceService.getAll())
            );
        } catch (Exception e) {
            return ResponseEntity.ok(
                    new ResponseObject(Status.ERROR, "Lỗi khi tìm lịch điểm danh: " + e.getMessage(), null)
            );
        }
    }
    @PostMapping("/check-in")
    @Operation(summary = "API điểm danh (check-in)")
    public ResponseEntity<ResponseObject> checkIn(@RequestBody AttendanceCheckInOutDTO attendanceRequest) {
        try {
            User user = userRepository.findById(attendanceRequest.getUserId())
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

            Attendance attendance = new Attendance();
            attendance.setUser(user);
            attendance.setCheckIn(attendanceRequest.getCheckIn());
            attendance.setStatus(attendanceRequest.getStatus());
            attendance.setIsActive(true);

            Attendance savedAttendance = attendanceService.save(attendance);

            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Điểm danh thành công", savedAttendance)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ResponseObject(Status.ERROR, "Lỗi khi điểm danh: " + e.getMessage(), null)
            );
        }
    }
    @PostMapping("/check-out")
    @Operation(summary = "API điểm danh (check-out)")
    public ResponseEntity<ResponseObject> checkOut(@RequestBody AttendanceCheckInOutDTO attendanceRequest) {
        try {
            User user = userRepository.findById(attendanceRequest.getUserId())
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

            LocalDateTime startOfDay = attendanceRequest.getCheckIn().toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);

            Attendance attendance = attendanceService.findByUserAndCheckInBetween(user, startOfDay, endOfDay)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bản ghi điểm danh cho người dùng"));

            attendance.setCheckOut(LocalDateTime.now());
            Attendance updatedAttendance = attendanceService.save(attendance);

            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Check-out thành công", updatedAttendance)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ResponseObject(Status.ERROR, "Lỗi khi check-out: " + e.getMessage(), null)
            );
        }
    }
}
