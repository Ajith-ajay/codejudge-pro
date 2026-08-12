package com.ajith.codejudge.question.dto.response;

import com.ajith.codejudge.question.entity.McqOption;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class McqQuestionResponse extends QuestionResponse {
    private List<McqOption> options;
    private boolean isMultipleChoice;
    private BigDecimal negativeMarking;
    private boolean partialMarking;
    private boolean randomizeOptions;
    private String explanation;
}
