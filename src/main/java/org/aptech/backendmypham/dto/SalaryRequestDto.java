package org.aptech.backendmypham.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SalaryRequestDto {
    private Long userId; // Giả sử ID của User là Long

    @Min(value = 1, message = "Tháng phải từ 1 đến 12")
    private Integer month;

    @Min(value = 2000, message = "Năm không hợp lệ")
    private Integer year;

    @DecimalMin(value = "0.0", inclusive = true, message = "Lương cơ bản không được âm")
    private BigDecimal baseSalary;

    @DecimalMin(value = "0.0", inclusive = true, message = "Thưởng không được âm")
    private BigDecimal bonus; // Mặc định là 0.00 từ entity

    @DecimalMin(value = "0.0", inclusive = true, message = "Khấu trừ không được âm")
    private BigDecimal deductions; // Mặc định là 0.00 từ entity

    @DecimalMin(value = "0.0", inclusive = true, message = "Tổng lương không được âm")
    private BigDecimal totalSalary; // Kết quả sau khi tính (Lương cơ bản + Thưởng - Khấu trừ)

    private LocalDate paymentDate;

    private String notes;

    private Boolean isActive;
}
