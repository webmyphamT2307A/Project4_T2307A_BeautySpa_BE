package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.aptech.backendmypham.dto.ScheduleUserDto;
import org.aptech.backendmypham.dto.UsersScheduleRequestDto;
import org.aptech.backendmypham.dto.UsersScheduleResponseDto;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.dto.ResponseObject;
import org.aptech.backendmypham.models.User;
import org.aptech.backendmypham.repositories.UserRepository;
import org.aptech.backendmypham.services.UsersScheduleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/users-schedules")
public class UsersScheduleController {

    private final UsersScheduleService usersScheduleService;
    private final UserRepository userRepository;
    @PostMapping("/created")
    @Operation(summary = "Tạo lịch làm việc cho nhân viên")
    public ResponseEntity<ResponseObject> createUsersSchedule(
                                                               @Valid @RequestBody UsersScheduleRequestDto requestDto) {
        UsersScheduleResponseDto createdSchedule = usersScheduleService.createSchedule(requestDto);
        ResponseObject responseObject =
                new ResponseObject(Status.SUCCESS, "Tạo lịch trình thành công.", createdSchedule);
        return new ResponseEntity<>(responseObject, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách lịch trình (có thể filter)")
    public ResponseEntity<ResponseObject> findSchedules(
                                                         @RequestParam(required = false) Long userId,
                                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                         @RequestParam(required = false) Integer month,
                                                         @RequestParam(required = false) Integer year,
                                                         @RequestParam(required = false) String status) { // Giữ nguyên kiểu String cho status filter, service sẽ xử lý

        List<UsersScheduleResponseDto> schedules = usersScheduleService.findSchedules(userId, startDate, endDate, month, year, status);
        String message = schedules.isEmpty() ? "Không tìm thấy lịch trình nào." : "Lấy danh sách lịch trình thành công.";
        ResponseObject responseObject =
                new ResponseObject(Status.SUCCESS, message, schedules);
        return ResponseEntity.ok(responseObject);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Lấy lịch trình theo User ID (có thể filter theo ngày tháng)")
    public ResponseEntity<ResponseObject> getSchedulesByUserId(
                                                                @PathVariable Long userId,
                                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                                @RequestParam(required = false) Integer month,
                                                                @RequestParam(required = false) Integer year) {
        List<UsersScheduleResponseDto> schedules = usersScheduleService.findSchedules(userId, startDate, endDate, month, year, null);
        String message = schedules.isEmpty() ? "Không tìm thấy lịch trình nào cho người dùng này." : "Lấy danh sách lịch trình của người dùng thành công.";
        // Sử dụng Status enum của bạn
        ResponseObject responseObject =
                new ResponseObject(Status.SUCCESS, message, schedules);
        return ResponseEntity.ok(responseObject);
    }

    @GetMapping("/find/{scheduleId}")
    @Operation(summary = "Lấy chi tiết lịch trình bằng Schedule ID")
    public ResponseEntity<ResponseObject> getUsersScheduleById(@PathVariable Integer scheduleId) {
        UsersScheduleResponseDto schedule = usersScheduleService.getScheduleById(scheduleId);
        // Sử dụng Status enum của bạn
        ResponseObject responseObject =
                new ResponseObject(Status.SUCCESS, "Lấy chi tiết lịch trình thành công.", schedule);
        return ResponseEntity.ok(responseObject);
    }

    @PutMapping("/update/{scheduleId}")
    @Operation(summary = "Cập nhật lịch trình")
    public ResponseEntity<ResponseObject> updateUsersSchedule(
                                                               @PathVariable Integer scheduleId,
                                                               @Valid @RequestBody UsersScheduleRequestDto requestDto) {
        UsersScheduleResponseDto updatedSchedule = usersScheduleService.updateSchedule(scheduleId, requestDto);
        // Sử dụng Status enum của bạn
        ResponseObject responseObject =
                new ResponseObject(Status.SUCCESS, "Cập nhật lịch trình thành công.", updatedSchedule);
        return ResponseEntity.ok(responseObject);
    }

    @PutMapping("/{scheduleId}")
    @Operation(summary = "Xóa (vô hiệu hóa) lịch trình")
    public ResponseEntity<ResponseObject> deleteUsersSchedule(@PathVariable Integer scheduleId) {
        usersScheduleService.deleteSchedule(scheduleId);
        ResponseObject responseObject =
                new ResponseObject(Status.SUCCESS, "Xóa lịch trình thành công (vô hiệu hóa).", null);
        return ResponseEntity.ok(responseObject);
    }
    @PutMapping("/check-in/{scheduleId}")
    @PreAuthorize("hasRole('STAFF')")
    @Operation(summary = "Nhân viên thực hiện Check-in (chấm công vào ca)")
    public ResponseEntity<ResponseObject> checkIn(@PathVariable Integer scheduleId, Principal principal) {
        Long staffId = getCurrentStaffId(principal);
        UsersScheduleResponseDto updatedSchedule = usersScheduleService.checkIn(scheduleId, staffId);
        ResponseObject responseObject =
                new ResponseObject(Status.SUCCESS, "Check-in thành công.", updatedSchedule);
        return ResponseEntity.ok(responseObject);
    }

    private Long getCurrentStaffId(Principal principal) {
        String email = principal.getName(); // Usually email from JWT

        // Look up staff user by email
        User staff = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Staff not found with email: " + email));
        System.out.println("Current staff ID: " + staff.getId());
        return staff.getId();
    }

    @PutMapping("/check-out/{scheduleId}")
    @Operation(summary = "Nhân viên thực hiện Check-out (chấm công tan ca)")
    public ResponseEntity<ResponseObject> checkOut(@PathVariable Integer scheduleId) {
        UsersScheduleResponseDto updatedSchedule = usersScheduleService.checkOut(scheduleId);
        ResponseObject responseObject =
                new ResponseObject(Status.SUCCESS, "Check-out thành công.", updatedSchedule);
        return ResponseEntity.ok(responseObject);
    }

    @GetMapping("/user/{userId}/schedule")
    @Operation(summary = "Lấy lịch trình của nhân viên theo User ID (use for app)")
    public List<ScheduleUserDto> getUserScheduleByUserId(
            @PathVariable Long userId) {
        List<ScheduleUserDto> schedules = usersScheduleService.getUserScheduleByUserId(userId);
       if(schedules.isEmpty()) {
           return List.of();
       }
       return schedules;
    }
}