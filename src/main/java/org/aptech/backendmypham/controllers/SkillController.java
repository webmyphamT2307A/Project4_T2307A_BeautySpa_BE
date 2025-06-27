package org.aptech.backendmypham.controllers;

import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.models.Skill;
import org.aptech.backendmypham.repositories.SkillRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
