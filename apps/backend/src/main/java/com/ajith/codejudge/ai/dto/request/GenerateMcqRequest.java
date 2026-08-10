package com.ajith.codejudge.ai.dto.request;

import com.ajith.codejudge.question.entity.Difficulty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class GenerateMcqRequest {

    @NotNull(message = "Skill ID is required")
    private Long skillId;

    @NotNull(message = "Difficulty is required")
    private Difficulty difficulty;

    @Min(value = 1, message = "Marks must be at least 1")
    @Max(value = 100, message = "Marks cannot exceed 100")
    private int marks = 1;

    private boolean multipleChoice = false;

    @DecimalMin(value = "0.0", message = "Negative marking cannot be negative")
    @Digits(integer = 3, fraction = 2, message = "Negative marking can have at most 2 decimal places")
    private java.math.BigDecimal negativeMarking = java.math.BigDecimal.ZERO;

    private boolean partialMarking = false;

    private boolean randomizeOptions = true;
}
