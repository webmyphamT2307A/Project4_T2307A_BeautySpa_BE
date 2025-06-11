package org.aptech.backendmypham.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShiftReportDto {
    private String name; // "Morning", "Afternoon", "Evening"
    private long count;  // Số lượng khách
    private double percentage;
}
