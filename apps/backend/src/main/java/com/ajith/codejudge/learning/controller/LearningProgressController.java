package com.ajith.codejudge.learning.controller;

import com.ajith.codejudge.common.response.ApiResponse;
import com.ajith.codejudge.learning.dto.response.SkillProgressResponse;
import com.ajith.codejudge.learning.service.SkillProgressService;
import com.ajith.codejudge.security.service.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/learning")
@RequiredArgsConstructor
@Tag(name = "Learning", description = "Adaptive learning progress")
public class LearningProgressController {

    private final SkillProgressService skillProgressService;

    @GetMapping("/progress")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'EXAM_SETTER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get the logged-in user's skill mastery")
    public ResponseEntity<ApiResponse<List<SkillProgressResponse>>> getProgress(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                skillProgressService.getUserProgress(userDetails.getId()),
                "Learning progress retrieved successfully"));
    }
}
