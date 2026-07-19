package com.ajith.codejudge.question.dto.response;

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
public class TestCaseResponse {
    private Long id;
    private String input;
    private String expectedOutput;
    private boolean hidden;
    private int marks;
}
