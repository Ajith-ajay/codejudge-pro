package com.ajith.codejudge.exam.service;

import com.ajith.codejudge.exam.dto.request.ActivityLogRequest;
import com.ajith.codejudge.exam.dto.response.ActivityLogResponse;
import com.ajith.codejudge.exam.entity.ActivityLog;
import com.ajith.codejudge.exam.entity.ExamCandidate;
import com.ajith.codejudge.exam.mapper.ActivityLogMapper;
import com.ajith.codejudge.exam.repository.ActivityLogRepository;
import com.ajith.codejudge.exam.repository.ExamCandidateRepository;
import com.ajith.codejudge.exam.service.impl.ActivityLogServiceImpl;
import com.ajith.codejudge.exception.ForbiddenException;
import com.ajith.codejudge.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityLogServiceTest {

    @Mock
    private ActivityLogRepository activityLogRepository;

    @Mock
    private ExamCandidateRepository examCandidateRepository;

    @Mock
    private ActivityLogMapper activityLogMapper;

    @InjectMocks
    private ActivityLogServiceImpl activityLogService;

    private User candidateUser;
    private ExamCandidate candidate;
    private ActivityLog activityLog;

    @BeforeEach
    void setUp() {
        candidateUser = User.builder()
                .id(1L)
                .username("testcandidate")
                .build();

        candidate = ExamCandidate.builder()
                .id(10L)
                .user(candidateUser)
                .build();

        activityLog = ActivityLog.builder()
                .id(50L)
                .candidate(candidate)
                .activityType("TAB_SWITCHED")
                .details("Switched tab to browser")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void logActivity_Success() {
        // Arrange
        ActivityLogRequest request = ActivityLogRequest.builder()
                .activityType("TAB_SWITCHED")
                .details("Switched tab to browser")
                .build();

        when(examCandidateRepository.findById(10L)).thenReturn(Optional.of(candidate));
        when(activityLogRepository.save(any(ActivityLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ActivityLogResponse responseDto = ActivityLogResponse.builder()
                .id(50L)
                .activityType("TAB_SWITCHED")
                .details("Switched tab to browser")
                .createdAt(LocalDateTime.now())
                .build();
        when(activityLogMapper.toResponse(any(ActivityLog.class))).thenReturn(responseDto);

        // Act
        ActivityLogResponse response = activityLogService.logActivity(10L, request, 1L); // userId 1 matches candidate user ID 1

        // Assert
        assertNotNull(response);
        assertEquals("TAB_SWITCHED", response.getActivityType());
        verify(activityLogRepository, times(1)).save(any(ActivityLog.class));
    }

    @Test
    void logActivity_ThrowsForbidden_WhenUserNotMatching() {
        // Arrange
        ActivityLogRequest request = ActivityLogRequest.builder()
                .activityType("TAB_SWITCHED")
                .build();

        when(examCandidateRepository.findById(10L)).thenReturn(Optional.of(candidate));

        // Act & Assert
        assertThrows(ForbiddenException.class, () ->
                activityLogService.logActivity(10L, request, 2L) // userId 2 does not match candidate user ID 1
        );
        verify(activityLogRepository, never()).save(any(ActivityLog.class));
    }

    @Test
    void getLogsByCandidate_ReturnsList() {
        // Arrange
        when(activityLogRepository.findByCandidateIdOrderByCreatedAtDesc(10L))
                .thenReturn(Collections.singletonList(activityLog));
        
        ActivityLogResponse responseDto = ActivityLogResponse.builder()
                .id(50L)
                .activityType("TAB_SWITCHED")
                .build();
        when(activityLogMapper.toResponse(any(ActivityLog.class))).thenReturn(responseDto);

        // Act
        List<ActivityLogResponse> list = activityLogService.getLogsByCandidate(10L);

        // Assert
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("TAB_SWITCHED", list.get(0).getActivityType());
    }
}
