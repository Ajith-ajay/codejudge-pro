package com.ajith.codejudge.learning.session.controller;

import com.ajith.codejudge.common.response.ApiResponse;
import com.ajith.codejudge.learning.session.dto.response.*;
import com.ajith.codejudge.learning.session.service.DailyLearningSessionService;
import com.ajith.codejudge.security.service.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/learning/sessions")
@RequiredArgsConstructor
@Tag(name = "Daily Learning Sessions", description = "Daily adaptive roadmap execution")
public class DailyLearningSessionController {

    private final DailyLearningSessionService sessionService;

    @PostMapping("/today")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'EXAM_SETTER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Create or retrieve today's learning session")
    public ResponseEntity<ApiResponse<LearningSessionResponse>> today(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                sessionService.getOrCreateToday(userDetails.getId()),
                "Today's learning session retrieved successfully"));
    }

    @GetMapping("/current")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'EXAM_SETTER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LearningSessionResponse>> current(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                sessionService.getCurrent(userDetails.getId()),
                "Current learning session retrieved successfully"));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'EXAM_SETTER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LearningDashboardResponse>> dashboard(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                sessionService.getDashboard(userDetails.getId()),
                "Learning dashboard retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'EXAM_SETTER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LearningSessionResponse>> get(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                sessionService.get(userDetails.getId(), id),
                "Learning session retrieved successfully"));
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'EXAM_SETTER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LearningSessionResponse>> start(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                sessionService.start(userDetails.getId(), id),
                "Learning session started successfully"));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'EXAM_SETTER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LearningSessionResponse>> complete(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                sessionService.complete(userDetails.getId(), id),
                "Learning session completed successfully"));
    }

    @PostMapping("/activities/{activityId}/start")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'EXAM_SETTER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LearningSessionResponse>> startActivity(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long activityId) {
        return ResponseEntity.ok(ApiResponse.success(
                sessionService.startActivity(userDetails.getId(), activityId),
                "Learning activity started successfully"));
    }

    @PostMapping("/activities/{activityId}/complete")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'EXAM_SETTER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LearningSessionResponse>> completeActivity(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long activityId) {
        return ResponseEntity.ok(ApiResponse.success(
                sessionService.completeActivity(userDetails.getId(), activityId),
                "Learning activity completed successfully"));
    }
}
