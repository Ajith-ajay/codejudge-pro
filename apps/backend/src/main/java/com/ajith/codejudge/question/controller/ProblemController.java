package com.ajith.codejudge.question.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ajith.codejudge.common.pagination.PageRequestDto;
import com.ajith.codejudge.common.pagination.PageResponseDto;
import com.ajith.codejudge.common.response.ApiResponse;
import com.ajith.codejudge.question.dto.response.ProblemListResponse;
import com.ajith.codejudge.question.service.interfaces.ProblemService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/problems")
@RequiredArgsConstructor
@Tag(name = "Problems", description = "LeetCode-style coding problem listing")
public class ProblemController {

    private final ProblemService problemService;

    @GetMapping
    @PreAuthorize(
            "hasAnyRole("
            + "'CANDIDATE', "
            + "'SUPER_ADMIN', "
            + "'ADMIN', "
            + "'EXAM_SETTER'"
            + ")"
    )
    @Operation(summary = "Get paginated coding problems with user progress and statistics")
    public ResponseEntity<
            ApiResponse<PageResponseDto<ProblemListResponse>>> getProblems(
            @Valid PageRequestDto pageRequest,
            @Parameter(description = "EASY, MEDIUM or HARD")
            @RequestParam(required = false) String difficulty,
            @Parameter(description = "SOLVED, ATTEMPTED or NOT_ATTEMPTED")
            @RequestParam(required = false) String status,
            @Parameter(description = "Search by problem title")
            @RequestParam(required = false) String search
    ) {

        PageResponseDto<ProblemListResponse> response
                = problemService.getProblems(
                        pageRequest,
                        difficulty,
                        status,
                        search
                );

        return ResponseEntity.ok(
                ApiResponse.success(response, "Problems retrieved successfully"
                )
        );
    }
}
