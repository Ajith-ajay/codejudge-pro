package com.ajith.codejudge.learning.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ajith.codejudge.learning.entity.AssessmentQuestion;
import com.ajith.codejudge.learning.entity.AssessmentQuestionId;

public interface AssessmentQuestionRepository extends JpaRepository<AssessmentQuestion, AssessmentQuestionId> {

    // boolean existsByIdAssessmentIdAndIdQuestionId(Long assessmentId, Long questionId);
    boolean existsByAssessmentIdAndQuestionId(Long assessmentId, Long questionId);
}
