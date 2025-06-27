package org.aptech.backendmypham.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewServiceAndStaffRequestDTO {

    @NotNull(message = "Service ID is required")
    private Integer serviceId;

    @NotNull(message = "Staff ID is required")
    private Long staffId;

    @NotNull(message = "Service rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer serviceRating;

    @NotNull(message = "Staff rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer staffRating;

    private String comment;
}