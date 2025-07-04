package org.aptech.backendmypham.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponseDTO {
    private Integer id;
    private Integer customerId;
    private Integer rating;
    private String comment;
    private Instant createdAt;
    private String authorName;

    private List<ReplyResponseDTO> replies;
}