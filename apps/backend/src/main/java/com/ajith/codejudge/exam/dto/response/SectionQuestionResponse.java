package com.ajith.codejudge.exam.dto.response;

import com.ajith.codejudge.question.dto.response.QuestionResponse;
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
public class SectionQuestionResponse {
    private QuestionResponse question;
    private int orderIndex;
}
