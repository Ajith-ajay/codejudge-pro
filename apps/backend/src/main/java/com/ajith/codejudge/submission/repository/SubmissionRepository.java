package com.ajith.codejudge.submission.repository;

import com.ajith.codejudge.submission.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    List<Submission> findByCandidateId(Long candidateId);

    List<Submission> findByQuestionId(Long questionId);

    List<Submission> findByCandidateIdAndQuestionId(Long candidateId, Long questionId);

    List<Submission> findByUserId(Long userId);

    List<Submission> findByUserIdAndCandidateIsNullAndQuestionIdInAndCreatedAtAfter(
            Long userId, List<Long> questionIds, LocalDateTime createdAt);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(DISTINCT s.question.id) FROM Submission s WHERE s.user.id = :userId AND s.candidate IS NULL AND s.status = com.ajith.codejudge.submission.entity.SubmissionStatus.ACCEPTED")
    long countSolvedQuestions(@org.springframework.data.repository.query.Param("userId") Long userId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(DISTINCT s.question.id) FROM Submission s WHERE s.user.id = :userId AND s.candidate IS NULL " +
           "AND s.question.id NOT IN (" +
           "  SELECT DISTINCT s2.question.id FROM Submission s2 WHERE s2.user.id = :userId AND s2.candidate IS NULL AND s2.status = com.ajith.codejudge.submission.entity.SubmissionStatus.ACCEPTED" +
           ")")
    long countAttemptedButUnsolvedQuestions(@org.springframework.data.repository.query.Param("userId") Long userId);

    @org.springframework.data.jpa.repository.Query(value = "WITH user_scores AS (" +
           "  SELECT user_id, SUM(max_score) AS total_score FROM (" +
           "    SELECT user_id, question_id, MAX(score) AS max_score FROM submissions WHERE candidate_id IS NULL GROUP BY user_id, question_id" +
           "  ) sub GROUP BY user_id" +
           ") " +
           "SELECT COUNT(*) + 1 FROM user_scores WHERE total_score > COALESCE((SELECT total_score FROM user_scores WHERE user_id = :userId), 0)",
           nativeQuery = true)
    int findGlobalRankByUserId(@org.springframework.data.repository.query.Param("userId") Long userId);
}
