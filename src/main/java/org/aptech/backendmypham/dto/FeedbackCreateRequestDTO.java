package org.aptech.backendmypham.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackCreateRequestDTO {
    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100)
    private String email;

    @Size(max = 20)
    private String phone;

    @NotBlank(message = "Subject is required")
    @Size(max = 255)
    private String subject;

    @NotBlank(message = "Message is required")
    private String message;
}
