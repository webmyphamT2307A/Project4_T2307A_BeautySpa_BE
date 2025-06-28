package org.aptech.backendmypham.controllers;

import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.SkillDTO;
import org.aptech.backendmypham.models.Skill;
import org.aptech.backendmypham.repositories.SkillRepository;
import org.aptech.backendmypham.repositories.UserSkillRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/skills")
@RequiredArgsConstructor
public class SkillController {
    public final SkillRepository skillRepository;
    public final UserSkillRepository userSkillRepository;

    @GetMapping("")
    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }

    @PostMapping("")
    public ResponseEntity<Skill> createSkill(@RequestBody SkillDTO skillDTO) {
        if (skillDTO.getSkillName() == null || skillDTO.getSkillName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên kỹ năng không được để trống");
        }
        Skill newSkill = new Skill();
        newSkill.setSkillName(skillDTO.getSkillName());
        newSkill.setDescription(skillDTO.getSkillDescription());
        newSkill.setActive(skillDTO.getActive());
        Skill savedSkill = skillRepository.save(newSkill);
        return ResponseEntity.ok(savedSkill);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Skill> updateSkill(@PathVariable Long id, @RequestBody SkillDTO skillDTO) {
        Skill existingSkill = skillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kỹ năng với ID: " + id));

        if (skillDTO.getSkillName() == null || skillDTO.getSkillName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên kỹ năng không được để trống");
        }

        existingSkill.setSkillName(skillDTO.getSkillName());
        existingSkill.setDescription(skillDTO.getSkillDescription());
        existingSkill.setActive(skillDTO.getActive());
        Skill updatedSkill = skillRepository.save(existingSkill);
        return ResponseEntity.ok(updatedSkill);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSkill(@PathVariable Long id) {
        skillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kỹ năng với ID: " + id));

        // 1. Xóa tất cả các liên kết user-skill của kỹ năng này
        userSkillRepository.deleteAllBySkill_Id(id);

        // 2. Thực hiện xóa mềm kỹ năng bằng cách cập nhật flag (nếu có)
        // Nếu không có trường active, bạn có thể xóa cứng bằng skillRepository.deleteById(id)
        Skill skill = skillRepository.getById(id);
        skill.setActive(false);
        skillRepository.save(skill);

        return ResponseEntity.noContent().build();
    }
}
