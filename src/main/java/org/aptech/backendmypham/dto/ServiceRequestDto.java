package org.aptech.backendmypham.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceRequestDto {
    private String name;
    private String description;
    private BigDecimal price;
    private Integer duration;
    private String imageUrl;
}
