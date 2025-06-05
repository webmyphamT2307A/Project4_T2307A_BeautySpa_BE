package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.aptech.backendmypham.dto.CalculateSalaryRequestDto;
import org.aptech.backendmypham.dto.ResponseObject;
import org.aptech.backendmypham.dto.SalaryRequestDto;
import org.aptech.backendmypham.dto.SalaryResponseDto;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.services.SalaryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/salaries")
@AllArgsConstructor
public class SalaryController {

    private final SalaryService salaryService;

    @PostMapping("/calculate")
    @Operation(summary = "Tính toán và lưu trữ lương cho một nhân viên trong một kỳ")
    public ResponseEntity<ResponseObject> calculateAndSaveSalary(
            @Valid @RequestBody CalculateSalaryRequestDto calculateDto) {
        SalaryResponseDto salaryResponse = salaryService.calculateAndSaveSalary(calculateDto);
        ResponseObject responseObject = new ResponseObject(
                Status.SUCCESS,
                "Tính toán và lưu lương thành công cho User ID: " + calculateDto.getUserId() +
                        " tháng " + calculateDto.getMonth() + "/" + calculateDto.getYear(),
                salaryResponse
        );
        return new ResponseEntity<>(responseObject, HttpStatus.CREATED);
    }

    @GetMapping("/find/{salaryId}")
    @Operation(summary = "Lấy thông tin chi tiết một bản ghi lương theo ID")
    public ResponseEntity<ResponseObject> getSalaryById(@PathVariable Integer salaryId) {
        SalaryResponseDto salaryResponse = salaryService.getSalaryById(salaryId);
        ResponseObject responseObject = new ResponseObject(
                Status.SUCCESS,
                "Lấy thông tin lương thành công cho ID: " + salaryId,
                salaryResponse
        );
        return ResponseEntity.ok(responseObject);
    }

    @GetMapping("/findSalary")
    @Operation(summary = "Lấy danh sách các bản ghi lương (có thể filter)")
    public ResponseEntity<ResponseObject> findSalaries(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        List<SalaryResponseDto> salaries = salaryService.findSalaries(userId, month, year);
        String message = salaries.isEmpty() ? "Không tìm thấy bản ghi lương nào." : "Lấy danh sách lương thành công.";
        ResponseObject responseObject = new ResponseObject(
                Status.SUCCESS,
                message,
                salaries
        );
        return ResponseEntity.ok(responseObject);
    }

    @PutMapping("/update/{salaryId}")
    @Operation(summary = "Cập nhật thông tin một bản ghi lương")
    public ResponseEntity<ResponseObject> updateSalary(
            @PathVariable Integer salaryId,
            @Valid @RequestBody SalaryRequestDto requestDto) {
        SalaryResponseDto updatedSalary = salaryService.updateSalary(salaryId, requestDto);
        ResponseObject responseObject = new ResponseObject(
                Status.SUCCESS,
                "Cập nhật bản ghi lương ID: " + salaryId + " thành công.",
                updatedSalary
        );
        return ResponseEntity.ok(responseObject);
    }

    @PutMapping("/delete/{salaryId}")
    @Operation(summary = "Xóa (vô hiệu hóa) một bản ghi lương")
    public ResponseEntity<ResponseObject> deleteSalary(@PathVariable Integer salaryId) {
        salaryService.deleteSalary(salaryId);
        ResponseObject responseObject = new ResponseObject(
                Status.SUCCESS,
                "Xóa (vô hiệu hóa) bản ghi lương ID: " + salaryId + " thành công.",
                null // Không có data trả về khi xóa
        );
        return ResponseEntity.ok(responseObject); // Hoặc HttpStatus.NO_CONTENT nếu không có body
    }
}