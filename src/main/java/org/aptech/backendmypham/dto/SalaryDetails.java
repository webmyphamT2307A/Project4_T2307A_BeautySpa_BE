package org.aptech.backendmypham.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class SalaryDetails {
    private Long baseSalary;
    private int workedDays;
    private int totalWorkdays;
    private Long totalHours;
    private Long totalTip;
    private double totalSalary;
}
