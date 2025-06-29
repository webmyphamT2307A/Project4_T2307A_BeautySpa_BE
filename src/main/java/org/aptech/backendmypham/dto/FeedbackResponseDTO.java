package org.aptech.backendmypham.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackResponseDTO {
    private Integer id;
    private String message;
    private Instant createdAt;
    private String customerName;
    private Integer customerId;
    private boolean isActive;
}
