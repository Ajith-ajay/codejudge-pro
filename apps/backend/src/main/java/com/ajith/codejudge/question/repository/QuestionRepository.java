package com.ajith.codejudge.question.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ajith.codejudge.question.entity.Question;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("SELECT q FROM Question q WHERE q.id NOT IN (" +
           "SELECT sq.question.id FROM SectionQuestion sq WHERE sq.section.exam.endTime > :now" +
           ")")
    Page<Question> findPracticeQuestions(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT q FROM Question q WHERE q.id NOT IN (" +
           "  SELECT sq.question.id FROM SectionQuestion sq WHERE sq.section.exam.endTime > :now" +
           ") AND q.id NOT IN (" +
           "  SELECT DISTINCT s.question.id FROM Submission s WHERE s.user.id = :userId AND s.candidate IS NULL AND s.status = com.ajith.codejudge.submission.entity.SubmissionStatus.ACCEPTED" +
           ")")
    List<Question> findRecommendedQuestions(@Param("now") LocalDateTime now, @Param("userId") Long userId, Pageable pageable);
}
