package com.ajith.codejudge.learning.session.dto.response;

import com.ajith.codejudge.learning.roadmap.entity.RoadmapActivityType;
import com.ajith.codejudge.learning.session.entity.LearningSessionActivityStatus;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter @Builder
public class LearningSessionActivityResponse {
    private Long id;
    private int sequenceNo;
    private RoadmapActivityType type;
    private String instructions;
    private int estimatedMinutes;
    private LearningSessionActivityStatus status;
    private Double score;
    private List<LearningSessionQuestionResponse> questions;
}
