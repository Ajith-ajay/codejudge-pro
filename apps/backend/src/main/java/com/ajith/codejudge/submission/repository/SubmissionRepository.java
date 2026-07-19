package com.ajith.codejudge.submission.repository;

import com.ajith.codejudge.submission.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    List<Submission> findByCandidateId(Long candidateId);

    List<Submission> findByQuestionId(Long questionId);

    List<Submission> findByCandidateIdAndQuestionId(Long candidateId, Long questionId);

    List<Submission> findByUserId(Long userId);
}
