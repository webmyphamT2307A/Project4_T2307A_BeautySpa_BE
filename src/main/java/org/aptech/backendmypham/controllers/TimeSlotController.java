package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.ResponseObject;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.services.TimeSlotService;
import org.aptech.backendmypham.repositories.UsersScheduleRepository;
import org.springframework.format.annotation.DateTimeFormat;
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
                new ResponseObject(Status.SUCCESS,"Lấy thành công",timeSlotService.getALlTimeSlot())
        );
    }

    @GetMapping("/available")
    @Operation(summary = "Kiểm tra số nhân viên có lịch làm việc thực tế")
    public ResponseEntity<ResponseObject> getAvailableSlot(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam Long serviceId,
            @RequestParam Long timeSlotId
    ) {
        // Get actual staff count with schedules on this date
        int totalStaffWithSchedule = usersScheduleRepository.countStaffWithScheduleOnDate(date);

        // Get available slots (staff count - booked appointments)
        int availableSlots = timeSlotService.getAvailableSlot(date, serviceId, timeSlotId);

        Map<String, Object> data = new HashMap<>();
        data.put("availableSlot", availableSlots);
        data.put("totalSlot", totalStaffWithSchedule); // Real staff count instead of fixed 10
        data.put("staffCount", totalStaffWithSchedule); // Additional clarity
        data.put("bookedSlots", totalStaffWithSchedule - availableSlots);
        data.put("message", totalStaffWithSchedule > 0 ?
                String.format("%d nhân viên có lịch làm việc, %d slot còn trống", totalStaffWithSchedule, availableSlots) :
                "Không có nhân viên nào có lịch làm việc vào ngày này");

        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS, "Thành công", data)
        );
    }

    @GetMapping("/staff-count")
    @Operation(summary = "Đếm số nhân viên có lịch làm việc theo ngày")
    public ResponseEntity<ResponseObject> getStaffCountByDate(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date
    ) {
        int staffCount = usersScheduleRepository.countStaffWithScheduleOnDate(date);

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