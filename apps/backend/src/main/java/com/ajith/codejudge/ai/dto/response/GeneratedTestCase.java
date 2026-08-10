package com.ajith.codejudge.ai.dto.response;

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
public class GeneratedTestCase {

    private String input;
    private String expectedOutput;
    private boolean hidden;
    private int marks;
}
