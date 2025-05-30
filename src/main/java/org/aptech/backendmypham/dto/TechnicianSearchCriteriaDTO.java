package org.aptech.backendmypham.dto;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class TechnicianSearchCriteriaDTO {
    private List<Long> skillIds;
    private Boolean requireAllSkills; // true: AND skills, false: OR skills
    private String skillKeyword; // Tìm trong skillName hoặc description (nếu có)
    private Double minAverageRating;
    private Integer minTotalReviews;
    // private String sortBy; // ví dụ: "rating_desc", "experience_asc" (sẽ dùng Pageable của Spring)
    // private String experienceLevel; // "junior", "senior",vv.
    // roleName có thể được cố định trong logic service nếu endpoint này chỉ dành cho Technician
}