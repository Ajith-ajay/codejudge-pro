package com.ajith.codejudge.exam.dto.response;

import com.ajith.codejudge.exam.entity.CandidateStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamCandidateResponse {
    private Long id;
    private Long examId;
    private String examTitle;
    private Long userId;
    private String username;
    private String email;
    private LocalDateTime invitedAt;
    private LocalDateTime joinedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private BigDecimal score;
    private Boolean passed;
    private CandidateStatus status;
}
