package com.ajith.codejudge.exam.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionRequest {

    @NotBlank(message = "Section title is required")
    private String title;

    private String description;

    @Min(value = 0, message = "Order index must be 0 or greater")
    private int orderIndex;

    private List<Long> questionIds;
}
