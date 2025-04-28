package org.aptech.backendmypham.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDetailResponseDto {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String imageUrl;
    private Boolean isActive;
}
