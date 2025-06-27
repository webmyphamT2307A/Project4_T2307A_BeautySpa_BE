package org.aptech.backendmypham.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UserSkillDto {
    private Long userId;
    private List<Long> skillIds;

}
