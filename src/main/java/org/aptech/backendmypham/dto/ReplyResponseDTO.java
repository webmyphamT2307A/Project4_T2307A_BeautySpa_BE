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
public class ReplyResponseDTO {
    private Integer id;
    private String comment;
    private String replyType; // "STAFF_TO_CUSTOMER"
    private String authorName; // Staff/Admin name
    private Instant createdAt;

}