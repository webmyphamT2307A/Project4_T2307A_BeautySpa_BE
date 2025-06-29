package org.aptech.backendmypham.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentHistoryDTO {
    private Integer id;
    private Integer appointmentId;

    // Customer info
    private Integer customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;

    // Service info
    private Long serviceId;
    private String serviceName;
    private BigDecimal servicePrice;
    private Integer serviceDuration;

    // Staff info
    private Long userId;
    private String userName;
    private String userImageUrl;
    private Double userRating;


    // Appointment details
    private String appointmentDate; // DD/MM/YYYY format
    private String appointmentTime; // HH:mm - HH:mm
    private String slot;
    private String status;
    private String notes;
    private Boolean isActive;
    private String createdAt;

    // UI helpers
    private String statusText; // "Đã hoàn thành", "Đã hủy", etc.
    private String statusClassName; // "bg-success", "bg-danger", etc.
    private Boolean canCancel;
    private String displayDate; // "Thứ 2, 25/12/2023"

    private Boolean isFeedBack; // kiểm tra xem đã feedback chưa
}