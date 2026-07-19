package com.ajith.codejudge.submission.dto.response;

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
public class SubmissionTestCaseResponse {
    private Long id;
    private Long testCaseId;
    private boolean hidden;
    private String status;
    private int executionTimeMs;
    private int executionMemoryMb;
    private String output;
    private String errorMessage;
}
