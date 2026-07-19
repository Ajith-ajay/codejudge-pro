package com.ajith.codejudge.exam.service.interfaces;

import com.ajith.codejudge.exam.dto.response.LeaderboardEntryDto;

import java.time.LocalDateTime;
import java.util.List;

public interface LeaderboardService {

    void updateScore(Long examId, Long candidateId, double score, LocalDateTime lastSubmittedAt);

    List<LeaderboardEntryDto> getLeaderboard(Long examId);

    void clearLeaderboard(Long examId);
}
