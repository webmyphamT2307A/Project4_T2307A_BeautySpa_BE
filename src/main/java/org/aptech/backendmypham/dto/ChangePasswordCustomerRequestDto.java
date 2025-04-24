package org.aptech.backendmypham.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordCustomerRequestDto {
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;


}
