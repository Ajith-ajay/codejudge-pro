package com.ajith.codejudge.ai.dto.response;

import com.ajith.codejudge.question.entity.Difficulty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedProblem {

    private String title;
    private String description;
    private Difficulty difficulty;
    private String constraints;
    private int timeLimitMs;
    private int memoryLimitMb;
    private List<GeneratedTestCase> testCases;
    private String referenceSolution;
}
