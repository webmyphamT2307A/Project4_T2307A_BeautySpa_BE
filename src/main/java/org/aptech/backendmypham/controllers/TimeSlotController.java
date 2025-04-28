package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.ResponseObject;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.services.TimeSlotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
