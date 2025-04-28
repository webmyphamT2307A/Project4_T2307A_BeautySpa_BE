package org.aptech.backendmypham.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {
    private Integer id;
    private String fullName;
    private String phone;
    private String email;
    private String password;
    private String imageUrl;
    private String address;
    private Boolean isActive;
    private Instant createdAt;
}
