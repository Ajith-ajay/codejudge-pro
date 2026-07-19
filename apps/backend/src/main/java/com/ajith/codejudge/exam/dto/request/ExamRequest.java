package com.ajith.codejudge.exam.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamRequest {

    @NotBlank(message = "Exam title is required")
    private String title;

    private String description;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;

    @Min(value = 5, message = "Duration must be at least 5 minutes")
    private int durationMinutes;

    @NotNull(message = "Pass percentage is required")
    @DecimalMin(value = "0.0", message = "Pass percentage must be 0.0 or higher")
    @DecimalMax(value = "100.0", message = "Pass percentage must be 100.0 or lower")
    private BigDecimal passPercentage;

    @Valid
    private List<SectionRequest> sections;
}
