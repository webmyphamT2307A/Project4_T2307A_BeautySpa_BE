package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.aptech.backendmypham.dto.UsersScheduleRequestDto;
import org.aptech.backendmypham.dto.UsersScheduleResponseDto;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.dto.ResponseObject;
import org.aptech.backendmypham.services.UsersScheduleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/users-schedules")
public class UsersScheduleController {

    private final UsersScheduleService usersScheduleService;

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
}