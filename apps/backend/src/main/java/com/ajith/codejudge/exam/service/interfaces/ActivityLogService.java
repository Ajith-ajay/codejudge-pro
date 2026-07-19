package com.ajith.codejudge.exam.service.interfaces;

import com.ajith.codejudge.exam.dto.request.ActivityLogRequest;
import com.ajith.codejudge.exam.dto.response.ActivityLogResponse;

import java.util.List;

public interface ActivityLogService {

    ActivityLogResponse logActivity(Long candidateId, ActivityLogRequest request, Long userId);

    List<ActivityLogResponse> getLogsByCandidate(Long candidateId);

    List<ActivityLogResponse> getLogsByExam(Long examId);
}
