package com.ajith.codejudge.exam.service.impl;

import com.ajith.codejudge.exam.dto.response.LeaderboardEntryDto;
import com.ajith.codejudge.exam.entity.ExamCandidate;
import com.ajith.codejudge.exam.repository.ExamCandidateRepository;
import com.ajith.codejudge.exam.service.interfaces.LeaderboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardServiceImpl implements LeaderboardService {

    private final StringRedisTemplate redisTemplate;
    private final ExamCandidateRepository examCandidateRepository;

    private String getLeaderboardKey(Long examId) {
        return "leaderboard:exam:" + examId;
    }

    @Override
    public void updateScore(Long examId, Long candidateId, double score, LocalDateTime lastSubmittedAt) {
        try {
            String key = getLeaderboardKey(examId);
            long epochSeconds = lastSubmittedAt != null ? lastSubmittedAt.atZone(ZoneId.systemDefault()).toEpochSecond() : 0L;
            // Tie-breaker formula: earlier submission time gets a higher score modifier
            double timeBonus = 1.0 - ((double) epochSeconds / 100_000_000_000.0);
            double redisScore = score + timeBonus;

            redisTemplate.opsForZSet().add(key, String.valueOf(candidateId), redisScore);
            log.info("Updated Redis leaderboard key {} for candidateId {} with score {}", key, candidateId, redisScore);
        } catch (Exception e) {
            log.error("Failed to update Redis leaderboard for exam {} candidate {}. Redis might be offline.", examId, candidateId, e);
        }
    }

    @Override
    public List<LeaderboardEntryDto> getLeaderboard(Long examId) {
        String key = getLeaderboardKey(examId);
        Set<ZSetOperations.TypedTuple<String>> range = null;

        try {
            range = redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, -1);
        } catch (Exception e) {
            log.error("Redis connection failed during leaderboard fetch for exam {}. Falling back to PostgreSQL database.", examId, e);
        }

        if (range == null || range.isEmpty()) {
            return getLeaderboardFromDatabase(examId);
        }

        List<LeaderboardEntryDto> entries = new ArrayList<>();
        int rank = 1;

        List<Long> candidateIds = range.stream()
                .map(ZSetOperations.TypedTuple::getValue)
                .map(Long::valueOf)
                .collect(Collectors.toList());

        List<ExamCandidate> candidates = examCandidateRepository.findAllById(candidateIds);
        Map<Long, ExamCandidate> candidateMap = candidates.stream()
                .collect(Collectors.toMap(ExamCandidate::getId, c -> c));

        for (ZSetOperations.TypedTuple<String> tuple : range) {
            Long candidateId = Long.valueOf(tuple.getValue());
            Double redisScore = tuple.getScore();
            if (redisScore == null) continue;

            ExamCandidate candidate = candidateMap.get(candidateId);
            if (candidate == null) continue;

            double score = Math.floor(redisScore);
            double timePart = 1.0 - (redisScore - score);
            long epochSeconds = Math.round(timePart * 100_000_000_000.0);
            LocalDateTime lastSubmittedAt = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(epochSeconds),
                    ZoneId.systemDefault()
            );

            entries.add(LeaderboardEntryDto.builder()
                    .rank(rank++)
                    .userId(candidate.getUser().getId())
                    .username(candidate.getUser().getUsername())
                    .email(candidate.getUser().getEmail())
                    .score(score)
                    .lastSubmittedAt(lastSubmittedAt)
                    .build());
        }

        return entries;
    }

    @Override
    public void clearLeaderboard(Long examId) {
        try {
            String key = getLeaderboardKey(examId);
            redisTemplate.delete(key);
            log.info("Cleared Redis leaderboard key {}", key);
        } catch (Exception e) {
            log.error("Failed to clear Redis leaderboard for exam {}", examId, e);
        }
    }

    private List<LeaderboardEntryDto> getLeaderboardFromDatabase(Long examId) {
        log.info("Fetching leaderboard from PostgreSQL database fallback for exam {}", examId);
        List<ExamCandidate> candidates = examCandidateRepository.findByExamId(examId);

        // Sort in memory: score desc, then last submitted/completed time asc
        List<ExamCandidate> sortedList = new ArrayList<>(candidates);
        sortedList.sort((c1, c2) -> {
            java.math.BigDecimal s1 = c1.getScore() != null ? c1.getScore() : java.math.BigDecimal.ZERO;
            java.math.BigDecimal s2 = c2.getScore() != null ? c2.getScore() : java.math.BigDecimal.ZERO;
            int scoreCompare = s2.compareTo(s1);
            if (scoreCompare != 0) {
                return scoreCompare;
            }
            LocalDateTime t1 = c1.getCompletedAt() != null ? c1.getCompletedAt() : c1.getStartedAt();
            LocalDateTime t2 = c2.getCompletedAt() != null ? c2.getCompletedAt() : c2.getStartedAt();
            if (t1 == null) return 1;
            if (t2 == null) return -1;
            return t1.compareTo(t2);
        });

        List<LeaderboardEntryDto> entries = new ArrayList<>();
        int rank = 1;
        for (ExamCandidate candidate : sortedList) {
            entries.add(LeaderboardEntryDto.builder()
                    .rank(rank++)
                    .userId(candidate.getUser().getId())
                    .username(candidate.getUser().getUsername())
                    .email(candidate.getUser().getEmail())
                    .score(candidate.getScore() != null ? candidate.getScore().doubleValue() : 0.0)
                    .lastSubmittedAt(candidate.getCompletedAt() != null ? candidate.getCompletedAt() : candidate.getStartedAt())
                    .build());
        }
        return entries;
    }
}
