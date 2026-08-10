package com.ajith.codejudge.learning.repository;

import com.ajith.codejudge.learning.entity.LearningAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LearningAssessmentRepository extends JpaRepository<LearningAssessment, Long> {
    Optional<LearningAssessment> findByIdAndUserId(Long id, Long userId);
}
