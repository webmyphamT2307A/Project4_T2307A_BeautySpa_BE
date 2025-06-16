package org.aptech.backendmypham.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReplyCreateRequestDTO {
    @NotEmpty(message = "Reply comment cannot be empty")
    private String comment;

}
