package com.ajith.codejudge.learning.dto.response;

import com.ajith.codejudge.question.entity.Difficulty;
import com.ajith.codejudge.question.entity.QuestionType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssessmentQuestionResponse {
    private int order;
    private Long questionId;
    private String title;
    private Difficulty difficulty;
    private QuestionType type;
}
