package org.aptech.backendmypham.repositories;

import org.aptech.backendmypham.models.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    List<Skill> findAllByIsActiveTrue();
    boolean existsBySkillName(String skillName);
}
