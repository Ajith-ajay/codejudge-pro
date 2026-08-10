package com.ajith.codejudge.learning.service;

import com.ajith.codejudge.learning.dto.request.CreateAdaptiveAssessmentRequest;
import com.ajith.codejudge.learning.dto.response.LearningAssessmentResponse;

public interface AdaptiveAssessmentService {
    LearningAssessmentResponse create(Long userId, CreateAdaptiveAssessmentRequest request);
    LearningAssessmentResponse get(Long userId, Long assessmentId);
    LearningAssessmentResponse start(Long userId, Long assessmentId);
    LearningAssessmentResponse complete(Long userId, Long assessmentId);
}
