package org.aptech.backendmypham.controllers;

import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.UserSkillDto;
import org.aptech.backendmypham.models.Skill;
import org.aptech.backendmypham.models.User;
import org.aptech.backendmypham.models.UserSkill;
import org.aptech.backendmypham.models.UserSkillId;
import org.aptech.backendmypham.repositories.SkillRepository;
import org.aptech.backendmypham.repositories.UserRepository;
import org.aptech.backendmypham.repositories.UserSkillRepository;
import org.springframework.web.bind.annotation.*;

        import java.util.List;

@RestController
@RequestMapping("/api/v1/user-skills")
@RequiredArgsConstructor
public class UserSkillController {
    private final UserSkillRepository userSkillRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    @GetMapping("")
    public List<UserSkill> getAllUserSkills() {
        return userSkillRepository.findAll();
    }

    @PostMapping("/insert")
    public void insertUserSkill(@RequestBody UserSkillDto userSkill) {
        List<Long> skillIds = userSkill.getSkillIds();
        Long userId = userSkill.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        for (Long skillId : skillIds) {
            Skill skill = skillRepository.findById(skillId)
                    .orElseThrow(() -> new RuntimeException("Skill not found with ID: " + skillId));

            UserSkillId id = new UserSkillId();
            id.setUserId(userId);
            id.setSkillId(skillId);

            UserSkill userSkillEntity = new UserSkill();
            userSkillEntity.setId(id);     // **Set id bắt buộc**
            userSkillEntity.setUser(user);
            userSkillEntity.setSkill(skill);

            userSkillRepository.save(userSkillEntity);
        }
    }

    @PutMapping("/edit")
    public void updateUserSkill(@RequestBody UserSkillDto userSkill) {
        List<Long> skillIds = userSkill.getSkillIds();
        Long userId = userSkill.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Xóa kỹ năng cũ
        List<UserSkill> existingSkills = userSkillRepository.findByUser_Id(userId);
        userSkillRepository.deleteAll(existingSkills);

        // Thêm kỹ năng mới
        for (Long skillId : skillIds) {
            Skill skill = skillRepository.findById(skillId)
                    .orElseThrow(() -> new RuntimeException("Skill not found with ID: " + skillId));

            UserSkillId id = new UserSkillId();
            id.setUserId(userId);
            id.setSkillId(skillId);

            UserSkill userSkillEntity = new UserSkill();
            userSkillEntity.setId(id);  // **Set id bắt buộc**
            userSkillEntity.setUser(user);
            userSkillEntity.setSkill(skill);

            userSkillRepository.save(userSkillEntity);
        }
    }

    @DeleteMapping("/delete/{userId}")
    public void deleteUserSkill(@PathVariable Long userId) {
        List<UserSkill> userSkills = userSkillRepository.findByUser_Id(userId);
        if (userSkills.isEmpty()) {
            throw new RuntimeException("No skills found for user with ID: " + userId);
        }
        userSkillRepository.deleteAll(userSkills);
    }
}