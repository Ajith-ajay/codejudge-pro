package com.ajith.codejudge.exam.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntryDto {
    private int rank;
    private Long userId;
    private String username;
    private String email;
    private double score;
    private LocalDateTime lastSubmittedAt;
}
