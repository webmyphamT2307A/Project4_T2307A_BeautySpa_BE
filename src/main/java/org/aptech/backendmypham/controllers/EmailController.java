package org.aptech.backendmypham.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aptech.backendmypham.dto.EmailConfirmationRequestDto;
import org.aptech.backendmypham.dto.ResponseObject;
import org.aptech.backendmypham.enums.Status; // <-- QUAN TRỌNG
import org.aptech.backendmypham.services.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/email")
@RequiredArgsConstructor
@Slf4j
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send-appointment-confirmation")
    public ResponseEntity<ResponseObject> sendAppointmentConfirmation(
            @Valid @RequestBody EmailConfirmationRequestDto request) {

        log.info("Request to send confirmation for appointment ID: {}", request.getAppointmentId());

        try {
            // Service sẽ throw Exception nếu thất bại, không trả về boolean nữa
            emailService.sendAppointmentConfirmation(request);

            log.info("Email sent successfully for appointment ID: {}", request.getAppointmentId());

            // Trả về response thành công sử dụng enum
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Appointment confirmation email sent successfully", null)
            );

        } catch (Exception e) {
            log.error("Error sending email for appointment ID: {}. Error: {}",
                    request.getAppointmentId(), e.getMessage(), e); // Thêm 'e' để log stack trace

            // Trả về response lỗi sử dụng enum
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject(Status.ERROR, "Failed to send email: " + e.getMessage(), null));
        }
    }

    @GetMapping("/test")
    public ResponseEntity<ResponseObject> testEmailService() {
        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS, "Email service is running", "Email API endpoints are available")
        );
    }
}