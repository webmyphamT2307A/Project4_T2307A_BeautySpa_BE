package org.aptech.backendmypham.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentStatsDTO {
    private Long totalAppointments;
    private Long completedAppointments;
    private Long cancelledAppointments;
    private Long upcomingAppointments;
    private BigDecimal totalSpent;
    private String lastAppointmentDate;
    private String mostUsedService;
    private String preferredStaff;
}