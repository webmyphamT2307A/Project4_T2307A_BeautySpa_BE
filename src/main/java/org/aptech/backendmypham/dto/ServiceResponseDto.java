package org.aptech.backendmypham.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceResponseDto {
    private Integer id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer duration;
    private String imageUrl;
    private Instant createdAt;
    private Boolean isActive;
}
