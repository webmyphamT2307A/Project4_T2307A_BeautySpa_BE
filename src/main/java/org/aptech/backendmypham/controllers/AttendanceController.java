package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.AttendanceCheckInOutDTO;
import org.aptech.backendmypham.dto.AttendanceHourDto;
import org.aptech.backendmypham.dto.ResponseObject;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.models.Attendance;
import org.aptech.backendmypham.models.User;
import org.aptech.backendmypham.repositories.UserRepository;
import org.aptech.backendmypham.services.AttendanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    //api tìm lịch điểm danh theo userId và trong phạm vi tuần hiện tại
    @GetMapping("/find-by-user/{userId}")
    @Operation(summary = "Tìm lịch điểm danh theo userId trong tuần hiện tại")
    public List<AttendanceHourDto> findByUserId(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

            LocalDateTime startOfWeek = LocalDateTime.now().with(java.time.DayOfWeek.MONDAY);
            LocalDateTime endOfWeek = startOfWeek.plusDays(6).withHour(23).withMinute(59).withSecond(59);

            List<AttendanceHourDto> attendances = attendanceService.findByUserAndBetween(user, startOfWeek, endOfWeek);
            return attendances;
        } catch (Exception e) {
            // Xử lý lỗi nếu không tìm thấy người dùng hoặc có lỗi khác
            System.err.println("Lỗi khi tìm lịch điểm danh: " + e.getMessage());
            return List.of();
        }
    }

//    api so sánh tổng thời gian làm tuần này so với tuần trước
    @GetMapping("/compare-week/{userId}")
    @Operation(summary = "So sánh tổng thời gian làm việc tuần này với tuần trước")
    public String compareWeek(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

            LocalDateTime startOfCurrentWeek = LocalDateTime.now().with(java.time.DayOfWeek.MONDAY);
            LocalDateTime endOfCurrentWeek = startOfCurrentWeek.plusDays(6).withHour(23).withMinute(59).withSecond(59);

            LocalDateTime startOfLastWeek = startOfCurrentWeek.minusWeeks(1);
            LocalDateTime endOfLastWeek = startOfLastWeek.plusDays(6).withHour(23).withMinute(59).withSecond(59);

            List<AttendanceHourDto> currentWeekAttendances = attendanceService.findByUserAndBetween(user, startOfCurrentWeek, endOfCurrentWeek);
            List<AttendanceHourDto> lastWeekAttendances = attendanceService.findByUserAndBetween(user, startOfLastWeek, endOfLastWeek);

            double currentWeekTotalHours = currentWeekAttendances.stream().mapToDouble(AttendanceHourDto::getTotalHours).sum();
            double lastWeekTotalHours = lastWeekAttendances.stream().mapToDouble(AttendanceHourDto::getTotalHours).sum();

            // mess dạng +5% so với tuần trước hoặc -5% so với tuần trước
            String comparisonMessage;
            double percentageChange = ((currentWeekTotalHours - lastWeekTotalHours) / lastWeekTotalHours) * 100;
            if (currentWeekTotalHours > lastWeekTotalHours) {
                comparisonMessage = String.format("+%.2f%% so với tuần trước", percentageChange);
            } else if (currentWeekTotalHours < lastWeekTotalHours) {
                comparisonMessage = String.format("-%.2f%% so với tuần trước", Math.abs(percentageChange));
            } else {
                comparisonMessage = "";
            }

            return comparisonMessage;
        } catch (Exception e) {
            // Xử lý lỗi nếu không tìm thấy người dùng hoặc có lỗi khác
            System.err.println("Lỗi khi so sánh tuần: " + e.getMessage());
            return "";
        }
    }

    @GetMapping("history")
    public List<Object> getAttendanceHistory() {
        return List.of();
    }
}
