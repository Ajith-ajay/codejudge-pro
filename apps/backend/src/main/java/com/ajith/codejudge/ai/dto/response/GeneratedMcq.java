package com.ajith.codejudge.ai.dto.response;

import com.ajith.codejudge.question.entity.Difficulty;
import java.math.BigDecimal;
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
public class GeneratedMcq {
    private String title;
    private String description;
    private Difficulty difficulty;
    private int marks;
    private List<GeneratedMcqOption> options;
    private boolean multipleChoice;
    private BigDecimal negativeMarking;
    private boolean partialMarking;
    private boolean randomizeOptions;
    private String explanation;
}
