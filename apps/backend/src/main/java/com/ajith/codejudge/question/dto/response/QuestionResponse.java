package com.ajith.codejudge.question.dto.response;

import com.ajith.codejudge.question.entity.Difficulty;
import com.ajith.codejudge.question.entity.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class QuestionResponse {
    private Long id;
    private String title;
    private String description;
    private Difficulty difficulty;
    private int marks;
    private QuestionType type;
}
