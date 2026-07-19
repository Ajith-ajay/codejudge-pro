package com.ajith.codejudge.question.repository;

import com.ajith.codejudge.question.entity.CodingQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CodingQuestionRepository extends JpaRepository<CodingQuestion, Long> {
}
