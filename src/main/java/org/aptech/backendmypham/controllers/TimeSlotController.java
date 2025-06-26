package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.ResponseObject;
import org.aptech.backendmypham.dto.TimeSlotDTO;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.services.TimeSlotService;
import org.aptech.backendmypham.repositories.UsersScheduleRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/timeslot")
@RequiredArgsConstructor
public class TimeSlotController {
    private final TimeSlotService timeSlotService;
    private final UsersScheduleRepository usersScheduleRepository;

    @GetMapping("")
    @Operation(summary = "Lấy tất cả trong timeslot")
    public ResponseEntity<ResponseObject> getAllTimeSlots() {
        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS, "Lấy thành công", timeSlotService.getALlTimeSlot())
        );
    }


    // Endpoint Tạo mới
    @PostMapping("/create")
    @Operation(summary = "Tạo một khung giờ mới")
    public ResponseEntity<ResponseObject> createTimeSlot(@Valid @RequestBody TimeSlotDTO timeSlotDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject(Status.SUCCESS, "Tạo khung giờ thành công", timeSlotService.createTimeSlot(timeSlotDTO))
        );
    }

    // Endpoint Cập nhật
    @PutMapping("/update/{id}")
    @Operation(summary = "Cập nhật một khung giờ")
    public ResponseEntity<ResponseObject> updateTimeSlot(@PathVariable Long id, @Valid @RequestBody TimeSlotDTO timeSlotDTO) {
        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS, "Cập nhật khung giờ thành công", timeSlotService.updateTimeSlot(id, timeSlotDTO))
        );
    }

    // Endpoint Xóa (mềm)
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Xóa (mềm) một khung giờ")
    public ResponseEntity<ResponseObject> deleteTimeSlot(@PathVariable Long id) {
        timeSlotService.deleteTimeSlot(id);
        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS, "Xóa khung giờ thành công", null)
        );
    }


    @GetMapping("/available")
    @Operation(summary = "Kiểm tra số slot còn trống và tổng số nhân viên làm việc")
    public ResponseEntity<ResponseObject> getAvailableSlot(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam Long serviceId, // serviceId hiện tại chưa dùng trong logic mới, nhưng giữ lại để có thể mở rộng sau
            @RequestParam Long timeSlotId
    ) {
        // Lấy dữ liệu từ service đã được sửa đổi
        int totalStaffWithSchedule = timeSlotService.getTotalStaffScheduled(date);
        int availableSlots = timeSlotService.getAvailableSlot(date, serviceId, timeSlotId);

        Map<String, Object> data = new HashMap<>();
        data.put("availableSlots", availableSlots);
        data.put("totalSlots", totalStaffWithSchedule);
        data.put("bookedSlots", totalStaffWithSchedule - availableSlots);
        data.put("message", totalStaffWithSchedule > 0 ?
                String.format("%d nhân viên có lịch làm việc, %d slot còn trống", totalStaffWithSchedule, availableSlots) :
                "Không có nhân viên nào có lịch làm việc vào ngày này");

        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS, "Thành công", data)
        );
    }

    // ✅ REWRITTEN LOGIC IN CONTROLLER
    @GetMapping("/staff-count")
    @Operation(summary = "Đếm số nhân viên có lịch làm việc theo ngày")
    public ResponseEntity<ResponseObject> getStaffCountByDate(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date
    ) {
        int staffCount = timeSlotService.getTotalStaffScheduled(date);

        Map<String, Object> data = new HashMap<>();
        data.put("date", date.toString());
        data.put("staffCount", staffCount);
        data.put("message", staffCount > 0 ?
                String.format("%d nhân viên có lịch làm việc vào ngày %s", staffCount, date) :
                String.format("Không có nhân viên nào có lịch làm việc vào ngày %s", date));

        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS, "Thành công", data)
        );

    }
}