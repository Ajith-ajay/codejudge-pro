package com.ajith.codejudge.skill.repository;

import com.ajith.codejudge.skill.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    List<Skill> findAllByActiveTrueOrderByCategoryAscNameAsc();
    Optional<Skill> findByNameIgnoreCase(String name);
}
