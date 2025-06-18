package org.aptech.backendmypham.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyReportDto {
    private String date;
    private long totalCustomers;
    private boolean isWeekend;
    private List<ShiftReportDto> shifts;
}
