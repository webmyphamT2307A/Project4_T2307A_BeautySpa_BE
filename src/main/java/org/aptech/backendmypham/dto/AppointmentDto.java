package org.aptech.backendmypham.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AppointmentDto {
    private Long userId;
    private Long serviceId;
    private Long customerId;
    private Long branchId;
    private Long timeSlotId;
    private String appointmentDate;
    private String slot;
    private String status;
    private String notes;
    private String phoneNumber;
    private String fullName;
    private Double price;
}
