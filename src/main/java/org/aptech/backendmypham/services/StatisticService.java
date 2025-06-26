package org.aptech.backendmypham.services;

import org.aptech.backendmypham.dto.*;

import java.time.LocalDate;
import java.util.List;

public interface StatisticService {
    DashboardSummaryDto getAdminDashboardSummary();
    DashboardSummaryDto getStaffDashboardSummary(Long userId);
    List<ChartDataDto> getRevenueByMonth(int year,Long userId);
    List<ChartDataDto> getRevenueByYear();
    List<RoleRatingDto> getRoleRatings();
    List<ChartDataDto> getCustomerCountByMonth(int year,Long userId);
    List<ChartDataDto> getCustomerCountByYear();
    List<ChartDataDto> getMyMonthlyRatings(int year, Long userId);
    List<DailyCustomerReportDto> getDailyCustomerReport(int year, int month);
    public DailyReportDto getDailyDetailedReport(LocalDate date);
}
