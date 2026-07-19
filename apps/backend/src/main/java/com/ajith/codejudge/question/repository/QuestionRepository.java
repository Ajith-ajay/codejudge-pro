package com.ajith.codejudge.question.repository;

import com.ajith.codejudge.question.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("SELECT q FROM Question q WHERE q.id NOT IN (" +
           "SELECT sq.question.id FROM SectionQuestion sq WHERE sq.section.exam.endTime > :now" +
           ")")
    Page<Question> findPracticeQuestions(@Param("now") LocalDateTime now, Pageable pageable);
}
