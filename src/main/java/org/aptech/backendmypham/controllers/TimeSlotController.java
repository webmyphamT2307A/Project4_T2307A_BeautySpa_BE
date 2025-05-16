package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.ResponseObject;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.services.TimeSlotService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/timeslot")
@RequiredArgsConstructor
public class TimeSlotController {
    private final TimeSlotService timeSlotService;

    @GetMapping(" ")
    @Operation(summary = "Lấy tất cả trong timeslot")
    public ResponseEntity<ResponseObject> getAllTimeSlots() {
        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS,"Lấy thành công",timeSlotService.getALlTimeSlot())
        );
    }
    @GetMapping("/available")
    @Operation(summary = "Giới hạn lượt đặt của khách hàng")
    public ResponseEntity<ResponseObject> getAvailableSlot(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam Long serviceId,
            @RequestParam Long timeSlotId
    ) {
        int available = timeSlotService.getAvailableSlot(date, serviceId, timeSlotId);
        int totalSlot = 10;
        Map<String, Integer> data = Map.of(
                "availableSlot", available,
                "totalSlot", totalSlot
        );
        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS, "Thành công", data)
        );
    }

}
