package com.ajith.codejudge.learning.dto.response;

import com.ajith.codejudge.learning.entity.LearningAssessmentStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class LearningAssessmentResponse {
    private Long id;
    private Long skillId;
    private String skillName;
    private LearningAssessmentStatus status;
    private int totalQuestions;
    private int mcqCount;
    private int codingCount;
    private String targetDifficulty;
    private Double score;
    private List<AssessmentQuestionResponse> questions;
}
