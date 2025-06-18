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
public class ReplyResponseDTO {
    private Integer id;
    private String comment;
    private String authorName;
    private String replyType;
    private Instant createdAt;
    private List<ReplyResponseDTO> replies; // Nested replies
}