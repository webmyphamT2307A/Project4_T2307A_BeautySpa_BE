package org.aptech.backendmypham.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DashboardSummaryDto {
    private long waitingCustomers;
    private long servedCustomersToday;
    private BigDecimal todayRevenue;
    private long servicesPerformedThisMonth;
    private double overallAverageRating;
}
