package org.aptech.backendmypham.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCustomerRequestDto {
    private Long id;
    private String fullName;
    private String phone;
    private String address;
    private String imageUrl;
}
