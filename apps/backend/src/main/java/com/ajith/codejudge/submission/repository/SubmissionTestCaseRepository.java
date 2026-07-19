package com.ajith.codejudge.submission.repository;

import com.ajith.codejudge.submission.entity.SubmissionTestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionTestCaseRepository extends JpaRepository<SubmissionTestCase, Long> {

    List<SubmissionTestCase> findBySubmissionId(Long submissionId);
}
