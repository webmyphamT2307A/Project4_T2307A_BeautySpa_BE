package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.BookingDTO;
import org.aptech.backendmypham.dto.ResponseObject;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.services.BookingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/booking")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @GetMapping("/staff-availability")
    @Operation(summary = "xem nhân viên có rảnh hay bận (maybe)")
    public ResponseEntity<ResponseObject> checkStaffAvailability(
            @RequestParam Integer userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant requestedDateTime, // VD: 2025-12-25T10:00:00Z
            @RequestParam Integer durationMinutes) {
        try {
            boolean isAvailable = bookingService.isStaffAvailable(userId, requestedDateTime, durationMinutes);

            Map<String, Object> dataPayload = new HashMap<>();
            dataPayload.put("userId", userId);
            dataPayload.put("requestedDateTime", requestedDateTime.toString());
            dataPayload.put("durationMinutes", durationMinutes);
            dataPayload.put("isAvailable", isAvailable);
            if (!isAvailable) {
                dataPayload.put("availabilityMessage", "Nhân viên đã có lịch vào thời điểm này.");
            } else {
                dataPayload.put("availabilityMessage", "Nhân viên sẵn sàng vào thời điểm này.");
            }

            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Kiểm tra lịch khả dụng của nhân viên thành công.", dataPayload)
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    new ResponseObject(Status.ERROR, e.getMessage(), null)
            );
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ResponseObject(Status.ERROR, "Đã xảy ra lỗi máy chủ khi kiểm tra lịch của nhân viên.", null)
            );
        }
    }
    @PostMapping("/create")
    @Operation(summary = "tạo booking ")
    public ResponseEntity<ResponseObject> createBooking(@RequestBody @Valid BookingDTO dto) {
        try {
            bookingService.createBooking(dto);
            return ResponseEntity.ok(new ResponseObject(Status.SUCCESS, "Đặt lịch thành công.", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject(Status.ERROR, "Đặt lịch thất bại: " + e.getMessage(), null));
        }
    }
}