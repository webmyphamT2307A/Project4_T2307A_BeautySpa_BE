package org.aptech.backendmypham.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class Schedule {
    private Long id;
    private LocalDate date;
    private String formattedDate;
    private String weekday;
    private String shift;
    private String checkInTime;
    private String checkOutTime;
    private String status;
    private Boolean isActive;
}