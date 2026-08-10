package com.ajith.codejudge.learning.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAdaptiveAssessmentRequest {

    private Long skillId;

    @Min(1)
    @Max(20)
    @Builder.Default
    private int mcqCount = 5;

    @Min(1)
    @Max(20)
    @Builder.Default
    private int codingCount = 5;
}
