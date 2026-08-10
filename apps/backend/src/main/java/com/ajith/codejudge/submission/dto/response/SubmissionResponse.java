package com.ajith.codejudge.submission.dto.response;

import com.ajith.codejudge.submission.entity.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionResponse {
    private Long id;
    private Long candidateId;
    private Long assessmentId;
    private Long questionId;
    private String questionTitle;
    private String languageCode;
    private String languageName;
    private String sourceCode;
    private SubmissionStatus status;
    private int executionTimeMs;
    private int executionMemoryMb;
    private int score;
    private LocalDateTime createdAt;
    private List<SubmissionTestCaseResponse> testCaseResults;
}
