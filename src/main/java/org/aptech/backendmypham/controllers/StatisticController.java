package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.ResponseObject;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.services.StatisticService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
public class StatisticController {

    private final StatisticService statisticService;

    @GetMapping("/summary")
    @Operation(summary = "Lấy các số liệu thống kê nhanh cho dashboard")
    public ResponseEntity<ResponseObject> getDashboardSummary() {
        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS, "Lấy dữ liệu tóm tắt thành công.", statisticService.getDashboardSummary())
        );
    }

    @GetMapping("/revenue-by-month")
    @Operation(summary = "Lấy dữ liệu doanh thu theo từng tháng trong năm")
    public ResponseEntity<ResponseObject> getRevenueByMonth(
            @RequestParam(required = false) Integer year) {
        // Nếu không truyền năm thì mặc định là năm hiện tại
        int targetYear = (year == null) ? LocalDate.now().getYear() : year;
        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS, "Lấy doanh thu theo tháng thành công.", statisticService.getRevenueByMonth(targetYear))
        );
    }

    @GetMapping("/revenue-by-year")
    @Operation(summary = "Lấy dữ liệu doanh thu theo từng năm")
    public ResponseEntity<ResponseObject> getRevenueByYear() {
        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS, "Lấy doanh thu theo năm thành công.", statisticService.getRevenueByYear())
        );
    }

    @GetMapping("/role-ratings")
    @Operation(summary = "Lấy đánh giá trung bình theo từng vai trò")
    public ResponseEntity<ResponseObject> getRoleRatings() {
        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS, "Lấy đánh giá theo vai trò thành công.", statisticService.getRoleRatings())
        );
    }
    @GetMapping("/customers-by-month")
    @Operation(summary = "Lấy dữ liệu số lượng khách hàng theo từng tháng trong năm")
    public ResponseEntity<ResponseObject> getCustomersByMonth(
            @RequestParam(required = false) Integer year) {
        int targetYear = (year == null) ? LocalDate.now().getYear() : year;
        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS, "Lấy số lượng khách hàng theo tháng thành công.", statisticService.getCustomerCountByMonth(targetYear))
        );
    }

    @GetMapping("/customers-by-year")
    @Operation(summary = "Lấy dữ liệu số lượng khách hàng theo từng năm")
    public ResponseEntity<ResponseObject> getCustomersByYear() {
        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS, "Lấy số lượng khách hàng theo năm thành công.", statisticService.getCustomerCountByYear())
        );
    }
}