package com.ajith.codejudge.learning.session.dto.response;

import com.ajith.codejudge.learning.session.entity.LearningSessionStatus;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Builder
public class LearningSessionResponse {
    private Long id;
    private Long roadmapId;
    private int roadmapDayNumber;
    private String roadmapDayTitle;
    private LocalDate sessionDate;
    private LearningSessionStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private int totalActivities;
    private int completedActivities;
    private int progressPercentage;
    private List<LearningSessionActivityResponse> activities;
}
