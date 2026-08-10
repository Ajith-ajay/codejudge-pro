package com.ajith.codejudge.ai.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ajith.codejudge.ai.dto.request.GenerateProblemRequest;
import com.ajith.codejudge.ai.dto.response.ProblemGenerationResponse;
import com.ajith.codejudge.ai.service.ProblemGenerationService;
import com.ajith.codejudge.common.response.ApiResponse;
import com.ajith.codejudge.question.dto.response.CodingQuestionResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/ai/problems")
@RequiredArgsConstructor
@Tag(name = "AI Problem Generation", description = "Generate and publish validated coding problems using an LLM")
public class ProblemGenerationController {

    private final ProblemGenerationService problemGenerationService;

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EXAM_SETTER', 'CANDIDATE')")
    @Operation(summary = "Generate and validate one coding problem using the configured LLM")
    public ResponseEntity<ApiResponse<ProblemGenerationResponse>> generate(
            @Valid @RequestBody GenerateProblemRequest request
    ) {
        ProblemGenerationResponse response = problemGenerationService.generate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "AI problem generated and validated successfully"));
    }

    @PostMapping("/{generationId}/publish")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EXAM_SETTER', 'CANDIDATE')")
    @Operation(summary = "Publish a validated AI-generated coding problem")
    public ResponseEntity<ApiResponse<CodingQuestionResponse>> publish(
            @PathVariable Long generationId
    ) {
        CodingQuestionResponse response = problemGenerationService.publish(generationId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "AI-generated coding problem published successfully"));
    }
}
