package com.ajith.codejudge.learning.roadmap.dto.response;

import com.ajith.codejudge.learning.roadmap.entity.RoadmapActivityType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoadmapActivityResponse {
    private int sequenceNo;
    private RoadmapActivityType activityType;
    private String instructions;
    private int estimatedMinutes;
    private java.util.List<SkillReferenceResponse> skills;
}
