package com.ajith.codejudge.question.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionSkillRequest {

    @NotEmpty(message = "At least one skill is required")
    private List<@NotNull Long> skillIds;
}
