package org.aptech.backendmypham.dto;

import lombok.*;

import java.util.Set;

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
    Integer roleId;
    Integer branchId;
    String description;
    Boolean isActive;
    private Set<Long> skillIds;
    Double averageRating;
    Integer totalReviews;
}
