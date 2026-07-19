package com.ajith.codejudge.question.dto.request;

import com.ajith.codejudge.question.entity.Difficulty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodingQuestionRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Difficulty is required")
    private Difficulty difficulty;

    @Min(value = 1, message = "Marks must be at least 1")
    private int marks;

    private String constraints;

    @Min(value = 100, message = "Time limit must be at least 100ms")
    private int timeLimitMs;

    @Min(value = 10, message = "Memory limit must be at least 10MB")
    private int memoryLimitMb;

    @NotEmpty(message = "At least one allowed language is required")
    private Set<Long> allowedLanguageIds;

    @NotEmpty(message = "Test cases are required")
    @Valid
    private List<TestCaseRequest> testCases;
}
