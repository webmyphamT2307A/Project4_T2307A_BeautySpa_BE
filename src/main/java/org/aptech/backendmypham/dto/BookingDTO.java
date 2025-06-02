package org.aptech.backendmypham.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
public class BookingDTO {

    private Integer id;

    private Long userId;

    private Long customerId;

    private Integer serviceId;

    private Instant bookingDateTime;

    @Size(max = 50, message = "Trạng thái phải có tối đa 50 ký tự")
    private String status;

    private String notes;

    private BigDecimal totalPrice;

    private Integer durationMinutes;

    private Instant createdAt;
    private Instant updatedAt;

    private Boolean isActive;



}