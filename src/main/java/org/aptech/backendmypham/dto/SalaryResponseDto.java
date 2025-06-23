package org.aptech.backendmypham.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SalaryResponseDto {
    private Integer id;
    private Long userId;
    private String userName;
    private String userEmail;

    private Integer month;
    private Integer year;
    private BigDecimal baseSalary;
    private BigDecimal bonus;
    private BigDecimal deductions;
    private BigDecimal totalSalary;
    private LocalDate paymentDate;
    private String notes;
    private Instant createdAt;
    private Boolean isActive;

}
