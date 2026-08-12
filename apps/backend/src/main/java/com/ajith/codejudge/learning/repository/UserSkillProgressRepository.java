package com.ajith.codejudge.learning.repository;

import com.ajith.codejudge.learning.entity.UserSkillProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserSkillProgressRepository extends JpaRepository<UserSkillProgress, Long> {
    Optional<UserSkillProgress> findByUserIdAndSkillId(Long userId, Long skillId);
    List<UserSkillProgress> findByUserIdOrderByMasteryScoreAsc(Long userId);
}
