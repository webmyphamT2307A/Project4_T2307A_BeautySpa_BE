package org.aptech.backendmypham.dto;
import lombok.Getter;
import lombok.Setter;
import org.aptech.backendmypham.models.User;
import java.time.Instant;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Getter
@Setter
public class TechnicianResponseDTO {
    private Integer id;
    private String fullName;
    private String imageUrl;
    private Double averageRating;
    private Integer totalReviews;
    private String roleName;
    private Set<SkillDTO> skills;
    private Instant createdAt;
    // private String description;
    // private Long yearsOfExperience; // Có thể tính từ createdAt

    public TechnicianResponseDTO() {}

    public TechnicianResponseDTO(User user) {
        this.id = Math.toIntExact(user.getId());
        this.fullName = user.getFullName();
        this.imageUrl = user.getImageUrl();
        this.averageRating = user.getAverageRating();
        this.totalReviews = user.getTotalReviews();
        this.createdAt = user.getCreatedAt();
        if (user.getRole() != null) {
            this.roleName = user.getRole().getName();
        }
        if (user.getSkills() != null && !user.getSkills().isEmpty()) {
            this.skills = user.getSkills().stream()
                    .map(skillEntity -> new SkillDTO(skillEntity.getId(), skillEntity.getSkillName(),skillEntity.getDescription(),skillEntity.getIsActive()))
                    .collect(Collectors.toSet());
        } else {
            this.skills = new HashSet<>();
        }
    }
}