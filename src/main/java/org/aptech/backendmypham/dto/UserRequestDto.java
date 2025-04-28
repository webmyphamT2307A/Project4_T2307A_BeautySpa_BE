package org.aptech.backendmypham.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDto {
    Long id;
    String fullName;
    String phone;
    String email;
    String password;
    String imageUrl;
    String address;
    int roleId;
    int branchId;
    String description;
}
