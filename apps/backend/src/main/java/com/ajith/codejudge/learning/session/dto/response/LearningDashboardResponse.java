package com.ajith.codejudge.learning.session.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;

@Getter @Builder
public class LearningDashboardResponse {
    private Long sessionId;
    private LocalDate sessionDate;
    private String sessionStatus;
    private int completedActivities;
    private int totalActivities;
    private int progressPercentage;
    private String nextActivity;
    private Long nextActivityId;
    private long completedSessions;
    private long currentStreak;
}
