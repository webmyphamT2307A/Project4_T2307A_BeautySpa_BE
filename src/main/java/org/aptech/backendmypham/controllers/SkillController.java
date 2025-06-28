package org.aptech.backendmypham.controllers;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.SkillDTO;
import org.aptech.backendmypham.models.Skill;
import org.aptech.backendmypham.repositories.SkillRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
        return skillRepository.findAllByIsActiveTrue();
    }

    @PostMapping("")
    public ResponseEntity<Skill> createSkill(@RequestBody SkillDTO skillDTO) {
        if (skillDTO.getSkillName() == null || skillDTO.getSkillName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên kỹ năng không được để trống");
        }
        if (skillRepository.existsBySkillName(skillDTO.getSkillName())) {
            throw new IllegalArgumentException("Kỹ năng với tên này đã tồn tại");
        }
        Skill newSkill = new Skill();
        newSkill.setSkillName(skillDTO.getSkillName());
        newSkill.setDescription(skillDTO.getSkillDescription());
        newSkill.setIsActive(true); // Mặc định kỹ năng mới được kích hoạt
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
        if (skillDTO.getSkillDescription() != null) {
            existingSkill.setDescription(skillDTO.getSkillDescription());
        }
        if(skillDTO.getIsActive() != null) {
            existingSkill.setIsActive(skillDTO.getIsActive());
        }
        Skill updatedSkill = skillRepository.save(existingSkill);
        return ResponseEntity.ok(updatedSkill);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteSkill(@PathVariable Long id) {
        // Tìm skill, nếu không thấy sẽ báo lỗi
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kỹ năng với ID: " + id));

        // Xóa các liên kết của skill này với user
        userSkillRepository.deleteAllBySkill_Id(id);

        skill.setIsActive(false);
        skillRepository.save(skill);

        // Trả về mã thành công
        return ResponseEntity.noContent().build();
    }
}
