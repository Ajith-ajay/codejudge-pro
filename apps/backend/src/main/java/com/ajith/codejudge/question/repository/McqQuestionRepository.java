package com.ajith.codejudge.question.repository;

import com.ajith.codejudge.question.entity.McqQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface McqQuestionRepository extends JpaRepository<McqQuestion, Long> {
}
