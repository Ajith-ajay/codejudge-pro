package com.ajith.codejudge.submission.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class SubmissionRequest {

    @NotNull(message = "Question ID is required")
    private Long questionId;

    @NotNull(message = "Language ID is required")
    private Long languageId;

    @NotBlank(message = "Source code is required")
    private String sourceCode;

    private Long candidateId;
}
