package org.aptech.backendmypham.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UsersScheduleResponseDto {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;

    private String shift;
    private LocalDate workDate;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private String status;
    private Boolean isLastTask;
    private Boolean isActive;



}
