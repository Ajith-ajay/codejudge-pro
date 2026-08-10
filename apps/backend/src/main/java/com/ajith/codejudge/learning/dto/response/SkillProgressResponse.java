package com.ajith.codejudge.learning.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SkillProgressResponse {
    private Long skillId;
    private String skillName;
    private String category;
    private double masteryScore;
    private double confidenceScore;
    private int attempts;
    private int correctAttempts;
    private int codingAttempts;
    private int codingCorrect;
    private int mcqAttempts;
    private int mcqCorrect;
}
