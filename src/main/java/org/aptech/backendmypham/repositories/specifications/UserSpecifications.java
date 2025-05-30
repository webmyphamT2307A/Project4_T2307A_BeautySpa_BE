package org.aptech.backendmypham.repositories.specifications;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.aptech.backendmypham.models.Skill;
import org.aptech.backendmypham.models.User;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class UserSpecifications {

    public static Specification<User> isActive() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isTrue(root.get("isActive"));
    }

    public static Specification<User> hasRole(String roleName) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("role").get("name"), roleName);
    }

    public static Specification<User> hasAnyOfSkills(List<Long> skillIds) {
        if (CollectionUtils.isEmpty(skillIds)) {
            return null;
        }
        return (root, query, criteriaBuilder) -> {
            Join<User, Skill> skillJoin = root.join("skills");
            query.distinct(true);
            return skillJoin.get("id").in(skillIds);
        };
    }

    public static Specification<User> hasMinimumAverageRating(Double minRating) {
        if (minRating == null) return null;
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("averageRating"), minRating);
    }

    public static Specification<User> hasMinimumTotalReviews(Integer minReviews) {
        if (minReviews == null) return null;
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("totalReviews"), minReviews);
    }

    // PHIÊN BẢN SỬ DỤNG criteriaBuilder.function("LOWER", ...)
    public static Specification<User> skillKeywordSearch(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        // pattern đã được chuyển sang chữ thường trong Java
        String pattern = "%" + keyword.toLowerCase() + "%";

        return (root, query, criteriaBuilder) -> {
            Join<User, Skill> skillJoin = root.join("skills", JoinType.LEFT);
            query.distinct(true);

            // Gọi hàm LOWER của DB cho cột skillName
            Expression<String> lowerSkillName = criteriaBuilder.function(
                    "LOWER",                         // Tên hàm SQL (LOWER là chuẩn)
                    String.class,                    // Kiểu trả về của hàm
                    skillJoin.get("skillName")       // Đối số (cột cần áp dụng)
            );

            // Gọi hàm LOWER của DB cho cột skillsText
            Expression<String> lowerSkillsText = criteriaBuilder.function(
                    "LOWER",
                    String.class,
                    root.get("skillsText")
            );

            // pattern bây giờ so sánh với kết quả của hàm LOWER() từ DB
            Predicate skillNameMatch = criteriaBuilder.like(
                    lowerSkillName,
                    pattern
            );

            Predicate skillsTextMatch = criteriaBuilder.like(
                    lowerSkillsText,
                    pattern
            );

            // Logic OR đơn giản thường là đủ, vì LIKE với NULL sẽ không khớp.
            return criteriaBuilder.or(skillNameMatch, skillsTextMatch);
//            return skillsTextMatch;
        };
    }
}