package org.aptech.backendmypham.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AttendanceCheckInOutDTO {
    private Long userId;
    private String status;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
}