package com.ajith.codejudge.learning.controller;

import com.ajith.codejudge.common.response.ApiResponse;
import com.ajith.codejudge.learning.dto.request.CreateAdaptiveAssessmentRequest;
import com.ajith.codejudge.learning.dto.response.LearningAssessmentResponse;
import com.ajith.codejudge.learning.service.AdaptiveAssessmentService;
import com.ajith.codejudge.security.service.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/learning/assessments")
@RequiredArgsConstructor
@Tag(name = "Adaptive Assessments", description = "Performance-based practice tests")
public class AdaptiveAssessmentController {

    private final AdaptiveAssessmentService assessmentService;

    @PostMapping("/adaptive")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'EXAM_SETTER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Generate an adaptive MCQ + coding assessment")
    public ResponseEntity<ApiResponse<LearningAssessmentResponse>> create(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody CreateAdaptiveAssessmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        assessmentService.create(userDetails.getId(), request),
                        "Adaptive assessment generated successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'EXAM_SETTER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get an adaptive assessment")
    public ResponseEntity<ApiResponse<LearningAssessmentResponse>> get(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                assessmentService.get(userDetails.getId(), id),
                "Assessment retrieved successfully"));
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'EXAM_SETTER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Start an adaptive learning assessment")
    public ResponseEntity<ApiResponse<LearningAssessmentResponse>> start(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                assessmentService.start(userDetails.getId(), id),
                "Adaptive assessment started successfully"));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'EXAM_SETTER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Complete an adaptive assessment")
    public ResponseEntity<ApiResponse<LearningAssessmentResponse>> complete(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                assessmentService.complete(userDetails.getId(), id),
                "Assessment completed successfully"));
    }
}
