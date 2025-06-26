package org.aptech.backendmypham.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequestDto {
    private String fullName;
    private String email;
    private String password;
    private String phone;
    private String address;
}
