package com.ajith.codejudge.ai.dto.response;

import com.ajith.codejudge.ai.entity.AiMcqGenerationStatus;
import com.ajith.codejudge.question.entity.Difficulty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McqGenerationResponse {
    private Long generationId;
    private AiMcqGenerationStatus status;
    private String skill;
    private Difficulty difficulty;
    private String title;
    private String description;
    private int marks;
    private List<GeneratedMcqOption> options;
    private boolean multipleChoice;
    private String explanation;
    private String validationMessage;
}
