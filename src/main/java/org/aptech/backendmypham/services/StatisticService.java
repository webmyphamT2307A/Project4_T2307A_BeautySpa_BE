package org.aptech.backendmypham.services;

import org.aptech.backendmypham.dto.ChartDataDto;
import org.aptech.backendmypham.dto.DashboardSummaryDto;
import org.aptech.backendmypham.dto.RoleRatingDto;

import java.util.List;

public interface StatisticService {
    DashboardSummaryDto getDashboardSummary();
    List<ChartDataDto> getRevenueByMonth(int year);
    List<ChartDataDto> getRevenueByYear();
    List<RoleRatingDto> getRoleRatings();
    List<ChartDataDto> getCustomerCountByMonth(int year);
    List<ChartDataDto> getCustomerCountByYear();
}
