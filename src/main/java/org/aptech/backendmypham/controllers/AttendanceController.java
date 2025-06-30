package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.AttendanceCheckInOutDTO;
import org.aptech.backendmypham.dto.AttendanceHourDto;
import org.aptech.backendmypham.dto.ResponseObject;
import org.aptech.backendmypham.dto.UsersScheduleResponseDto;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.models.Attendance;
import org.aptech.backendmypham.models.User;
import org.aptech.backendmypham.repositories.AttendanceRepository;
import org.aptech.backendmypham.repositories.UserRepository;
import org.aptech.backendmypham.services.AttendanceService;
import org.aptech.backendmypham.services.UserService;
import org.aptech.backendmypham.services.UsersScheduleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/attendance")
public class AttendanceController {
    final private AttendanceService attendanceService;
    private final UserRepository userRepository;

    private final UserService userService;
    private final UsersScheduleService userScheduleService;

    private final AttendanceRepository attendanceRepository;

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
    public List<AttendanceHourDto> findByUserId(@PathVariable Long userId, @RequestParam(name = "type") String type) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

            ZoneId zoneVN = ZoneId.of("Asia/Ho_Chi_Minh");
            ZonedDateTime start;
            ZonedDateTime end;

            switch (type.toLowerCase()) {
                case "month":
                    int year = ZonedDateTime.now(zoneVN).getYear();
                    start = ZonedDateTime.of(LocalDate.of(year, 1, 1).atStartOfDay(), zoneVN);
                    end = ZonedDateTime.of(LocalDate.of(year, 12, 31).atTime(23, 59, 59), zoneVN);
                    break;

                case "year":
                    int currentYear = ZonedDateTime.now(zoneVN).getYear();
                    start = ZonedDateTime.of(LocalDate.of(currentYear - 4, 1, 1).atStartOfDay(), zoneVN);
                    end = ZonedDateTime.of(LocalDate.of(currentYear, 12, 31).atTime(23, 59, 59), zoneVN);
                    break;

                case "day":
                case "week":
                default:
                    LocalDate today = LocalDate.now(zoneVN);
                    LocalDate monday = today.with(java.time.DayOfWeek.MONDAY);
                    LocalDate sunday = monday.plusDays(6);

                    start = ZonedDateTime.of(monday.atStartOfDay(), zoneVN);
                    end = ZonedDateTime.of(sunday.atTime(23, 59, 59), zoneVN);
                    break;
            }

            // convert về LocalDateTime nếu hàm service xử lý kiểu đó
            return attendanceService.findByUserAndBetween(
                    user,
                    start.toLocalDateTime(),
                    end.toLocalDateTime(),
                    type
            );

        } catch (Exception e) {
            System.err.println("Lỗi khi tìm lịch điểm danh: " + e.getMessage());
            return List.of();
        }
    }


    //    api so sánh tổng thời gian làm tuần này so với tuần trước
    @GetMapping("/compare/{userId}")
    @Operation(summary = "So sánh tổng thời gian làm việc hiện tại với giai đoạn trước đó")
    public String comparePeriod(@PathVariable Long userId, @RequestParam(name = "type") String type) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

            LocalDateTime startCurrent, endCurrent;
            LocalDateTime startPrev, endPrev;

            LocalDate now = LocalDate.now();

            switch (type.toLowerCase()) {
                case "month":
                    YearMonth currentMonth = YearMonth.now();
                    YearMonth lastMonth = currentMonth.minusMonths(1);

                    startCurrent = currentMonth.atDay(1).atStartOfDay();
                    endCurrent = currentMonth.atEndOfMonth().atTime(23, 59, 59);

                    startPrev = lastMonth.atDay(1).atStartOfDay();
                    endPrev = lastMonth.atEndOfMonth().atTime(23, 59, 59);
                    break;

                case "year":
                    int currentYear = now.getYear();
                    int lastYear = currentYear - 1;

                    startCurrent = LocalDateTime.of(currentYear, 1, 1, 0, 0);
                    endCurrent = LocalDateTime.of(currentYear, 12, 31, 23, 59, 59);

                    startPrev = LocalDateTime.of(lastYear, 1, 1, 0, 0);
                    endPrev = LocalDateTime.of(lastYear, 12, 31, 23, 59, 59);
                    break;

                case "week":
                case "day":
                default:
                    startCurrent = now.with(DayOfWeek.MONDAY).atStartOfDay();
                    endCurrent = startCurrent.plusDays(6).withHour(23).withMinute(59).withSecond(59);

                    startPrev = startCurrent.minusWeeks(1);
                    endPrev = startPrev.plusDays(6).withHour(23).withMinute(59).withSecond(59);
                    break;
            }

            List<AttendanceHourDto> current = attendanceService.findByUserAndBetween(user, startCurrent, endCurrent, type);
            List<AttendanceHourDto> previous = attendanceService.findByUserAndBetween(user, startPrev, endPrev, type);

            double currentTotal = current.stream().mapToDouble(AttendanceHourDto::getTotalHours).sum();
            double previousTotal = previous.stream().mapToDouble(AttendanceHourDto::getTotalHours).sum();

            if (previousTotal == 0 && currentTotal == 0) return "";
            if (previousTotal == 0) return "+100% so với giai đoạn trước";

            double percent = ((currentTotal - previousTotal) / previousTotal) * 100;
            String formatted = String.format("%+.2f%% so với %s trước", percent,
                    switch (type.toLowerCase()) {
                        case "month" -> "tháng";
                        case "year" -> "năm";
                        default -> "tuần";
                    });

            return formatted;
        } catch (Exception e) {
            System.err.println("Lỗi khi so sánh giai đoạn: " + e.getMessage());
            return "";
        }
    }

    @GetMapping("/history")
    @Operation(summary = "Lấy lịch sử điểm danh theo userId, năm, tháng, trạng thái và số lượng bản ghi")
    public ResponseEntity<ResponseObject> getAttendanceHistory(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer take
    ) {
        try {
            // Lấy toàn bộ dữ liệu
            List<Attendance> attendances = attendanceService.getAll();

            // Lọc theo điều kiện
            if (userId != null) {
                attendances = attendances.stream()
                        .filter(a -> a.getUser().getId().equals(userId))
                        .toList();
            }
            if (year != null) {
                attendances = attendances.stream()
                        .filter(a -> a.getCheckIn().getYear() == year)
                        .toList();
            }
            if (month != null) {
                attendances = attendances.stream()
                        .filter(a -> a.getCheckIn().getMonthValue() == month)
                        .toList();
            }
            if (status != null) {
                attendances = attendances.stream()
                        .filter(a -> a.getStatus().equalsIgnoreCase(status))
                        .toList();
            }

            // Sắp xếp giảm dần theo ngày checkIn
            attendances = attendances.stream()
                    .sorted((a1, a2) -> a2.getCheckIn().compareTo(a1.getCheckIn()))
                    .toList();

            // Giới hạn theo số lượng bản ghi nếu take != null
            if (take != null && take > 0 && take < attendances.size()) {
                attendances = attendances.subList(0, take);
            }

            // Map dữ liệu trả về
            List<Map<String, Object>> response = attendances.stream().map(a -> {
                Map<String, Object> record = Map.of(
                        "id", a.getAttendanceId(),
                        "date", a.getCheckIn().toLocalDate(),
                        "session", a.getCheckIn().getHour() < 12 ? "Sáng" : "Chiều",
                        "checkInTime", a.getCheckIn(),
                        "checkOutTime", a.getCheckOut(),
                        "status", a.getStatus()
                );
                return record;
            }).toList();

            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Lấy lịch sử điểm danh thành công", response)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ResponseObject(Status.ERROR, "Lỗi khi lấy lịch sử điểm danh: " + e.getMessage(), null)
            );
        }
    }

  
    @GetMapping("/punctuality-rate/{userId}")
    @Operation(summary = "Tính tỉ lệ đúng giờ check-in của nhân viên trong tháng hiện tại")
    public String getPunctualityRate(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

            // Tính thời gian đầu và cuối tháng
            LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
            LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);

            // Lấy danh sách điểm danh của user trong tháng hiện tại
            List<Attendance> attendances = attendanceRepository.findByUserAndCheckInBetweenAndStatus(user, startOfMonth, endOfMonth);

            // Đếm số lần đúng giờ theo status
            long onTimeCount = attendances.stream()
                    .filter(a -> "on_time".equalsIgnoreCase(a.getStatus()))
                    .count();

            double rate = attendances.isEmpty() ? 0.0 : (double) onTimeCount / attendances.size() * 100;

            return rate == 0.0 ? "0%" : String.format("%.2f%%", rate);
        } catch (Exception e) {
            return "";
        }
    }
}
