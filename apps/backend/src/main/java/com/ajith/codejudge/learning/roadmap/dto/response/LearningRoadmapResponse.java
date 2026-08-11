package com.ajith.codejudge.learning.roadmap.dto.response;

import com.ajith.codejudge.learning.roadmap.entity.LearningRoadmapStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class LearningRoadmapResponse {
    private Long id;
    private String title;
    private String summary;
    private String goal;
    private String targetRole;
    private String preferredLanguage;
    private int durationDays;
    private int dailyMinutes;
    private LearningRoadmapStatus status;
    private int version;
    private LocalDateTime createdAt;
    private List<RoadmapDayResponse> days;
}
