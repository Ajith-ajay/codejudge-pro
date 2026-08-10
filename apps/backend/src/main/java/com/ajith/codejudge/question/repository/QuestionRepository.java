package com.ajith.codejudge.question.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ajith.codejudge.question.entity.Difficulty;
import com.ajith.codejudge.question.entity.Question;
import com.ajith.codejudge.question.entity.QuestionType;
import com.ajith.codejudge.learning.entity.AssessmentQuestion;
import com.ajith.codejudge.exam.entity.SectionQuestion;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("""
        SELECT q FROM Question q
        WHERE q.id NOT IN (
            SELECT sq.question.id
            FROM SectionQuestion sq
            WHERE sq.section.exam.endTime > :now
        )
        """)
    Page<Question> findPracticeQuestions(
            @Param("now") LocalDateTime now,
            Pageable pageable);

    @Query("""
        SELECT DISTINCT q FROM Question q
        JOIN q.skills skill
        WHERE skill.id = :skillId
          AND q.type = :type
          AND q.difficulty = :difficulty

          AND q.id NOT IN (
              SELECT s.question.id
              FROM Submission s
              WHERE s.user.id = :userId
                AND s.candidate IS NULL
                AND s.status =
                    com.ajith.codejudge.submission.entity.SubmissionStatus.ACCEPTED
          )

          AND q.id NOT IN (
              SELECT aq.question.id
              FROM AssessmentQuestion aq
              WHERE aq.assessment.user.id = :userId
                AND aq.assessment.status IN (
                    com.ajith.codejudge.learning.entity.LearningAssessmentStatus.GENERATED,
                    com.ajith.codejudge.learning.entity.LearningAssessmentStatus.IN_PROGRESS
                )
          )

          AND q.id NOT IN (
              SELECT sq.question.id
              FROM SectionQuestion sq
              WHERE sq.section.exam.endTime > CURRENT_TIMESTAMP
          )
        """)
    List<Question> findAdaptiveCandidates(
            @Param("skillId") Long skillId,
            @Param("type") QuestionType type,
            @Param("difficulty") Difficulty difficulty,
            @Param("userId") Long userId);

    // Add @Query here, or rename this method to a valid
    // Spring Data derived-query method.
    @Query("""
       SELECT q
       FROM Question q
       WHERE q.id IS NOT NULL
       AND (:userId IS NULL OR q.id = q.id)
       AND (:now IS NULL OR q.id = q.id)
       ORDER BY q.id DESC
       """)
    List<Question> findRecommendedQuestions(
            @Param("now") LocalDateTime now,
            @Param("userId") Long userId,
            Pageable pageable
    );

}
