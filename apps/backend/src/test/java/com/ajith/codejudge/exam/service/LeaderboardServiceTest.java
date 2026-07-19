package com.ajith.codejudge.exam.service;

import com.ajith.codejudge.exam.dto.response.LeaderboardEntryDto;
import com.ajith.codejudge.exam.entity.Exam;
import com.ajith.codejudge.exam.entity.ExamCandidate;
import com.ajith.codejudge.exam.repository.ExamCandidateRepository;
import com.ajith.codejudge.exam.service.impl.LeaderboardServiceImpl;
import com.ajith.codejudge.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @Mock
    private ExamCandidateRepository examCandidateRepository;

    @InjectMocks
    private LeaderboardServiceImpl leaderboardService;

    private ExamCandidate candidate1;
    private User user1;
    private Exam exam;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .id(1L)
                .username("candidate1")
                .email("c1@test.com")
                .build();

        exam = Exam.builder()
                .id(100L)
                .title("Midterm exam")
                .build();

        candidate1 = ExamCandidate.builder()
                .id(10L)
                .exam(exam)
                .user(user1)
                .score(BigDecimal.valueOf(85))
                .startedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void updateScore_CallsRedisOps() {
        // Arrange
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);

        // Act
        leaderboardService.updateScore(100L, 10L, 85.0, LocalDateTime.now());

        // Assert
        verify(zSetOperations, times(1)).add(eq("leaderboard:exam:100"), eq("10"), anyDouble());
    }

    @Test
    void getLeaderboard_RetrievesFromRedis_Success() {
        // Arrange
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

        ZSetOperations.TypedTuple<String> tuple = mock(ZSetOperations.TypedTuple.class);
        when(tuple.getValue()).thenReturn("10");
        when(tuple.getScore()).thenReturn(85.9823); // Double encoded score + timeBonus
        Set<ZSetOperations.TypedTuple<String>> range = new HashSet<>();
        range.add(tuple);

        when(zSetOperations.reverseRangeWithScores("leaderboard:exam:100", 0, -1)).thenReturn(range);
        when(examCandidateRepository.findAllById(anyList())).thenReturn(Collections.singletonList(candidate1));

        // Act
        List<LeaderboardEntryDto> leaderboard = leaderboardService.getLeaderboard(100L);

        // Assert
        assertNotNull(leaderboard);
        assertEquals(1, leaderboard.size());
        LeaderboardEntryDto entry = leaderboard.get(0);
        assertEquals("candidate1", entry.getUsername());
        assertEquals(85.0, entry.getScore());
    }

    @Test
    void getLeaderboard_FallbackToDatabase_WhenRedisOffline() {
        // Arrange
        when(redisTemplate.opsForZSet()).thenThrow(new RuntimeException("Redis connection error"));
        when(examCandidateRepository.findByExamId(100L)).thenReturn(Collections.singletonList(candidate1));

        // Act
        List<LeaderboardEntryDto> leaderboard = leaderboardService.getLeaderboard(100L);

        // Assert
        assertNotNull(leaderboard);
        assertEquals(1, leaderboard.size());
        LeaderboardEntryDto entry = leaderboard.get(0);
        assertEquals("candidate1", entry.getUsername());
        assertEquals(85.0, entry.getScore());
        verify(examCandidateRepository, times(1)).findByExamId(100L);
    }
}
