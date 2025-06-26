package org.aptech.backendmypham.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class ReviewDTO {
    private Integer id;
    private String authorName;
    private Long customerId;
    private String type;
    private Integer relatedId;
    private Integer rating;
    private String comment;
    private Instant createdAt;
    private boolean isActive;
}
