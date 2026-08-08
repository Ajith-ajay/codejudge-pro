package com.ajith.codejudge.question.dto.response;

import com.ajith.codejudge.question.entity.Difficulty;

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
public class ProblemListResponse {

    private Long id;

    private String title;

    private Difficulty difficulty;

    private ProblemStatus status;

    private double acceptanceRate;

    private long solvedUsers;

    private long totalSubmissions;
}
