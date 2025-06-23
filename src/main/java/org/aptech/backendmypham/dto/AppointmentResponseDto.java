package org.aptech.backendmypham.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AppointmentResponseDto {
    private Integer id;
    private String fullName;
    private String phoneNumber;
    private String status;
    private String slot;
    private String notes;
    private String appointmentDate;
    private String endTime;
    private BigDecimal price;
    private String userName;

    private String serviceName;
    private String customerName;
    private String customerEmail;
    private String UserImageUrl;
    private String customerImageUrl;



}
