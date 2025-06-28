package org.aptech.backendmypham.controllers;

import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.SkillDTO;
import org.aptech.backendmypham.models.Skill;
import org.aptech.backendmypham.repositories.SkillRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/skills")
@RequiredArgsConstructor
public class SkillController {
    public final SkillRepository skillRepository;

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
        Skill savedSkill = skillRepository.save(newSkill);
        return ResponseEntity.ok(savedSkill);
    }
}
