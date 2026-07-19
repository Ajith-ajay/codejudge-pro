package com.ajith.codejudge.question.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CodingQuestionResponse extends QuestionResponse {
    private String constraints;
    private int timeLimitMs;
    private int memoryLimitMb;
    private List<LanguageResponse> allowedLanguages;
    private List<TestCaseResponse> testCases;
}
