package org.aptech.backendmypham.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewCreateRequestDTO {
    private Integer relatedId;

    private String type;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    @NotNull(message = "Rating is required")
    private Integer rating;
    @Size(max = 100, message = "Guest name cannot exceed 100 characters")
    private String guestName;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Guest email cannot exceed 100 characters")
    private String guestEmail;

    @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
    private String comment;
}
