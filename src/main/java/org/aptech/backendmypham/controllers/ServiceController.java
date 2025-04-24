package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.ResponseObject;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.services.ServicesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/service")
@RequiredArgsConstructor
public class ServiceController {
    private final ServicesService servicesService;
    @GetMapping("")
    @Operation(summary = "Lấy tất cả service trong hệ thống")
    public ResponseEntity<ResponseObject> getAllService() {
        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS, "Thành công", servicesService.gellALlService())
        );
    }
}
