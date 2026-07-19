package com.ajith.codejudge.exam.service.impl;

import com.ajith.codejudge.exam.dto.request.ActivityLogRequest;
import com.ajith.codejudge.exam.dto.response.ActivityLogResponse;
import com.ajith.codejudge.exam.entity.ActivityLog;
import com.ajith.codejudge.exam.entity.ExamCandidate;
import com.ajith.codejudge.exam.mapper.ActivityLogMapper;
import com.ajith.codejudge.exam.repository.ActivityLogRepository;
import com.ajith.codejudge.exam.repository.ExamCandidateRepository;
import com.ajith.codejudge.exam.service.interfaces.ActivityLogService;
import com.ajith.codejudge.exception.ForbiddenException;
import com.ajith.codejudge.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final ExamCandidateRepository examCandidateRepository;
    private final ActivityLogMapper activityLogMapper;

    @Override
    @Transactional
    public ActivityLogResponse logActivity(Long candidateId, ActivityLogRequest request, Long userId) {
        ExamCandidate candidate = examCandidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate session not found"));

        // Candidates can only log their own activities
        if (!candidate.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You cannot log activity for another candidate session");
        }

        ActivityLog logEntity = ActivityLog.builder()
                .candidate(candidate)
                .activityType(request.getActivityType())
                .details(request.getDetails())
                .build();

        logEntity = activityLogRepository.save(logEntity);
        return activityLogMapper.toResponse(logEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityLogResponse> getLogsByCandidate(Long candidateId) {
        return activityLogRepository.findByCandidateIdOrderByCreatedAtDesc(candidateId).stream()
                .map(activityLogMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityLogResponse> getLogsByExam(Long examId) {
        return activityLogRepository.findByCandidateExamIdOrderByCreatedAtDesc(examId).stream()
                .map(activityLogMapper::toResponse)
                .collect(Collectors.toList());
    }
}
