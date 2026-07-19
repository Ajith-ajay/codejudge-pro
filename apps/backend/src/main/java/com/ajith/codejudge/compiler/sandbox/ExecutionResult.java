package com.ajith.codejudge.compiler.sandbox;

import com.ajith.codejudge.submission.entity.SubmissionStatus;
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
public class ExecutionResult {
    private SubmissionStatus status;
    private String output;
    private String errorMessage;
    private int timeMs;
    private int memoryMb;
}
