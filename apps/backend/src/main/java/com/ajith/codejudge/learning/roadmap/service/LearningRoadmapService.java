package com.ajith.codejudge.learning.roadmap.service;

import com.ajith.codejudge.learning.roadmap.dto.request.CreateLearningRoadmapRequest;
import com.ajith.codejudge.learning.roadmap.dto.response.LearningRoadmapResponse;

public interface LearningRoadmapService {
    LearningRoadmapResponse generate(Long userId, CreateLearningRoadmapRequest request);
    LearningRoadmapResponse getCurrent(Long userId);
    LearningRoadmapResponse get(Long userId, Long roadmapId);
}
