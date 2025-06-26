package org.aptech.backendmypham.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleUserDto {
    private Long id;
    private LocalDate date;
    private String shift;
    private String status;
}
