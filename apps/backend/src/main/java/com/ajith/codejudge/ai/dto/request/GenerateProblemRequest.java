package com.ajith.codejudge.ai.dto.request;

import com.ajith.codejudge.question.entity.Difficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateProblemRequest {

    @NotNull(message = "Skill ID is required")
    private Long skillId;

    @NotNull(message = "Difficulty is required")
    private Difficulty difficulty;

    @NotBlank(message = "Language code is required")
    private String languageCode;
}
