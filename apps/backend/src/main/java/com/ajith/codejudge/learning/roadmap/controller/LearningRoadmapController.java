package com.ajith.codejudge.learning.roadmap.controller;

import com.ajith.codejudge.common.response.ApiResponse;
import com.ajith.codejudge.learning.roadmap.dto.request.CreateLearningRoadmapRequest;
import com.ajith.codejudge.learning.roadmap.dto.response.LearningRoadmapResponse;
import com.ajith.codejudge.learning.roadmap.service.LearningRoadmapService;
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
@RequestMapping("/api/v1/learning/roadmaps")
@RequiredArgsConstructor
@Tag(name = "AI Learning Roadmap", description = "Personalized placement learning roadmaps")
public class LearningRoadmapController {

    private final LearningRoadmapService roadmapService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CANDIDATE', 'EXAM_SETTER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Generate a personalized AI learning roadmap")
    public ResponseEntity<ApiResponse<LearningRoadmapResponse>> generate(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody CreateLearningRoadmapRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                roadmapService.generate(userDetails.getId(), request),
                "Personalized learning roadmap generated successfully"));
    }

    @GetMapping("/current")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'EXAM_SETTER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get the current active learning roadmap")
    public ResponseEntity<ApiResponse<LearningRoadmapResponse>> current(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                roadmapService.getCurrent(userDetails.getId()),
                "Current learning roadmap retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'EXAM_SETTER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get a learning roadmap")
    public ResponseEntity<ApiResponse<LearningRoadmapResponse>> get(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                roadmapService.get(userDetails.getId(), id),
                "Learning roadmap retrieved successfully"));
    }
}
