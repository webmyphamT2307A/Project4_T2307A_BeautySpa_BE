package org.aptech.backendmypham.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceDTO {
    private Integer attendanceId;
    private Integer userId;
    private String userName;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private String status;
    private LocalDateTime createdAt;
    private Boolean isActive;
}