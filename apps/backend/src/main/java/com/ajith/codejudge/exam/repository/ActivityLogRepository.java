package com.ajith.codejudge.exam.repository;

import com.ajith.codejudge.exam.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findByCandidateIdOrderByCreatedAtDesc(Long candidateId);

    List<ActivityLog> findByCandidateExamIdOrderByCreatedAtDesc(Long examId);
}
