package com.ajith.codejudge.learning.session.service;

import com.ajith.codejudge.learning.session.dto.response.*;

public interface DailyLearningSessionService {
    LearningSessionResponse getOrCreateToday(Long userId);
    LearningSessionResponse getCurrent(Long userId);
    LearningSessionResponse get(Long userId, Long sessionId);
    LearningSessionResponse start(Long userId, Long sessionId);
    LearningSessionResponse startActivity(Long userId, Long activityId);
    LearningSessionResponse completeActivity(Long userId, Long activityId);
    LearningSessionResponse complete(Long userId, Long sessionId);
    LearningDashboardResponse getDashboard(Long userId);
}
