package com.ajith.codejudge.ai.dto.response;

import com.ajith.codejudge.ai.entity.AiProblemGenerationStatus;
import com.ajith.codejudge.question.entity.Difficulty;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemGenerationResponse {

    private Long generationId;
    private AiProblemGenerationStatus status;
    private String skill;
    private Difficulty difficulty;
    private String languageCode;
    private String title;
    private String description;
    private String constraints;
    private int timeLimitMs;
    private int memoryLimitMb;
    private int testCaseCount;
    private String validationMessage;
}
