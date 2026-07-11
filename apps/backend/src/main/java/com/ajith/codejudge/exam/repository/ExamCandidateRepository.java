package com.ajith.codejudge.exam.repository;

import com.ajith.codejudge.exam.entity.ExamCandidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamCandidateRepository extends JpaRepository<ExamCandidate, Long> {

    Optional<ExamCandidate> findByExamIdAndUserId(Long examId, Long userId);

    List<ExamCandidate> findByUserId(Long userId);

    List<ExamCandidate> findByExamId(Long examId);

    boolean existsByExamIdAndUserId(Long examId, Long userId);
}
