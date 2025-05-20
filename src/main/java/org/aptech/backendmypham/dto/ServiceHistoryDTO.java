package org.aptech.backendmypham.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceHistoryDTO {
    private Integer id;
    private Integer userId;
    private Integer customerId;
    private Integer appointmentId;
    private Integer serviceId;
    private String serviceName;
    private BigDecimal price;
    private Instant appointmentDate;
    private String notes;
    private Instant createdAt;
    private Boolean isActive;
}