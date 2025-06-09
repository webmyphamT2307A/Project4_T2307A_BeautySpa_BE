package org.aptech.backendmypham.services;

import org.aptech.backendmypham.dto.ChartDataDto;
import org.aptech.backendmypham.dto.DailyCustomerReportDto;
import org.aptech.backendmypham.dto.DashboardSummaryDto;
import org.aptech.backendmypham.dto.RoleRatingDto;

import java.util.List;

public interface StatisticService {
    DashboardSummaryDto getDashboardSummary(Long userId);
    List<ChartDataDto> getRevenueByMonth(int year,Long userId);
    List<ChartDataDto> getRevenueByYear();
    List<RoleRatingDto> getRoleRatings();
    List<ChartDataDto> getCustomerCountByMonth(int year,Long userId);
    List<ChartDataDto> getCustomerCountByYear();
    List<ChartDataDto> getMyMonthlyRatings(int year, Long userId);
    List<DailyCustomerReportDto> getDailyCustomerReport(int year, int month);
}
