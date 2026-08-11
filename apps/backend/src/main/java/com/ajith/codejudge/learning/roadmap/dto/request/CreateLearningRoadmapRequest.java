package com.ajith.codejudge.learning.roadmap.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLearningRoadmapRequest {

    @NotBlank(message = "Goal is required")
    @Size(max = 255, message = "Goal must be at most 255 characters")
    private String goal;

    @NotBlank(message = "Target role is required")
    @Size(max = 100, message = "Target role must be at most 100 characters")
    private String targetRole;

    @NotBlank(message = "Preferred language is required")
    @Size(max = 30, message = "Preferred language must be at most 30 characters")
    private String preferredLanguage;

    @Min(value = 1, message = "Duration must be at least 1 day")
    @Max(value = 180, message = "Duration cannot exceed 180 days")
    private int durationDays;

    @Min(value = 15, message = "Daily study time must be at least 15 minutes")
    @Max(value = 480, message = "Daily study time cannot exceed 480 minutes")
    private int dailyMinutes;
}
