package org.aptech.backendmypham.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DailyCustomerReportDto {
    private String date; // Định dạng "yyyy-MM-dd"
    private long count; // Tổng số khách trong ngày
    private List<ShiftReportDto> shifts;
}
