package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.ChartDataDto;
import org.aptech.backendmypham.dto.ResponseObject;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.services.StatisticService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
public class StatisticController {

    private final StatisticService statisticService;

    @GetMapping("/summary")
    @Operation(summary = "Lấy các số liệu thống kê nhanh cho dashboard (Admin/Nhân viên)")
    public ResponseEntity<ResponseObject> getDashboardSummary(
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS, "Lấy dữ liệu tóm tắt thành công.", statisticService.getDashboardSummary(userId))
        );
    }

    @GetMapping("/revenue-by-month")
    @Operation(summary = "Lấy dữ liệu doanh thu theo tháng (Admin/Nhân viên)")
    public ResponseEntity<ResponseObject> getRevenueByMonth(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Long userId) {
        int targetYear = (year == null) ? LocalDate.now().getYear() : year;
        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS, "Lấy doanh thu theo tháng thành công.", statisticService.getRevenueByMonth(targetYear, userId))
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
    @Operation(summary = "Lấy dữ liệu số lượng khách hàng theo tháng (Admin/Nhân viên)")
    public ResponseEntity<ResponseObject> getCustomersByMonth(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Long userId) { 
        int targetYear = (year == null) ? LocalDate.now().getYear() : year;
        // Gọi service với đủ tham số
        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS, "Lấy số lượng khách hàng theo tháng thành công.", statisticService.getCustomerCountByMonth(targetYear, userId))
        );
    }

    @GetMapping("/customers-by-year")
    @Operation(summary = "Lấy dữ liệu số lượng khách hàng theo từng năm")
    public ResponseEntity<ResponseObject> getCustomersByYear() {
        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS, "Lấy số lượng khách hàng theo năm thành công.", statisticService.getCustomerCountByYear())
        );
    }
    @GetMapping("/my-monthly-ratings")
    @Operation(summary = "Lấy dữ liệu rating hàng tháng của một nhân viên cho biểu đồ")
    public ResponseEntity<ResponseObject> getMyMonthlyRatings(
            @RequestParam Long userId,
            @RequestParam(required = false) Integer year) {

        // Nếu frontend không gửi năm, mặc định lấy năm hiện tại
        int targetYear = (year == null) ? LocalDate.now().getYear() : year;

        List<ChartDataDto> data = statisticService.getMyMonthlyRatings(targetYear, userId);

        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS, "Lấy rating hàng tháng thành công.", data)
        );
    }
    @GetMapping("/daily-customer-report")
    @Operation(summary = "Lấy báo cáo khách hàng hàng ngày theo ca (cho lịch)")
    public ResponseEntity<ResponseObject> getDailyCustomerReport(
            @RequestParam Integer year,
            @RequestParam Integer month) {

        int targetYear = (year == null) ? LocalDate.now().getYear() : year;
        int targetMonth = (month == null) ? LocalDate.now().getMonthValue() : month;

        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS,
                        "Lấy báo cáo khách hàng hàng ngày thành công.",
                        statisticService.getDailyCustomerReport(targetYear, targetMonth))
        );
    }
}