package org.aptech.backendmypham.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class CalculateSalaryRequestDto {
    private Long userId;

    @Min(value = 1, message = "Tháng phải từ 1 đến 12")
    private Integer month;

    @Min(value = 2000, message = "Năm không hợp lệ")
    private Integer year;

    // Các khoản thưởng/khấu trừ có thể được nhập tay để ghi đè
    // hoặc bổ sung vào quá trình tính toán tự động
    @DecimalMin(value = "0.0", inclusive = true, message = "Thưởng nhập tay không được âm")
    private BigDecimal manualBonus;

    @DecimalMin(value = "0.0", inclusive = true, message = "Khấu trừ nhập tay không được âm")
    private BigDecimal manualDeductions;

    private String notesForCalculation; // Ghi chú riêng cho lần tính lương này

}
