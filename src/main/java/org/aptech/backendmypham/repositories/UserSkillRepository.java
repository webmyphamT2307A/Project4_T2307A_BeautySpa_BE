package org.aptech.backendmypham.repositories;

import jakarta.transaction.Transactional;
import org.aptech.backendmypham.models.UserSkill;
import org.aptech.backendmypham.models.UserSkillId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, UserSkillId> {

    List<UserSkill> findByUser_Id(Long userId);

    List<UserSkill> findBySkill_Id(Long skillId);

    @Modifying
    @Transactional
    @Query("update Skill s set s.isActive = false where s.id = ?1")
    void deleteAllBySkill_Id(Long id);
}