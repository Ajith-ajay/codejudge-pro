package com.ajith.codejudge.question.dto.request;

import com.ajith.codejudge.question.entity.Difficulty;
import com.ajith.codejudge.question.entity.McqOption;
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

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McqQuestionRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Difficulty is required")
    private Difficulty difficulty;

    @Min(value = 1, message = "Marks must be at least 1")
    private int marks;

    @NotEmpty(message = "Options are required")
    @Valid
    private List<McqOption> options;

    private boolean isMultipleChoice;

    @NotNull(message = "Negative marking value is required")
    @Min(value = 0, message = "Negative marking must be 0 or greater")
    private BigDecimal negativeMarking;

    private boolean partialMarking;

    private boolean randomizeOptions;

    private String explanation;
}
