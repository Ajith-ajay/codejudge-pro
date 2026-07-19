package com.ajith.codejudge.question.repository;

import com.ajith.codejudge.question.entity.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Long> {

    List<TestCase> findByCodingQuestionId(Long codingQuestionId);
}
