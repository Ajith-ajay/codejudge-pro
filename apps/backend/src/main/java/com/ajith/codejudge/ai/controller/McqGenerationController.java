package com.ajith.codejudge.ai.controller;

import com.ajith.codejudge.ai.dto.request.GenerateMcqRequest;
import com.ajith.codejudge.ai.dto.response.McqGenerationResponse;
import com.ajith.codejudge.ai.service.McqGenerationService;
import com.ajith.codejudge.common.response.ApiResponse;
import com.ajith.codejudge.question.dto.response.McqQuestionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai/mcqs")
@RequiredArgsConstructor
@Tag(name = "AI MCQ Generation", description = "Generate and publish validated placement MCQs using an LLM")
public class McqGenerationController {

    private final McqGenerationService mcqGenerationService;

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EXAM_SETTER')")
    @Operation(summary = "Generate and validate one MCQ using the configured LLM")
    public ResponseEntity<ApiResponse<McqGenerationResponse>> generate(
            @Valid @RequestBody GenerateMcqRequest request) {
        McqGenerationResponse response = mcqGenerationService.generate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "AI MCQ generated and validated successfully"));
    }

    @PostMapping("/{generationId}/publish")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EXAM_SETTER')")
    @Operation(summary = "Publish a validated AI-generated MCQ")
    public ResponseEntity<ApiResponse<McqQuestionResponse>> publish(
            @PathVariable Long generationId) {
        McqQuestionResponse response = mcqGenerationService.publish(generationId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "AI-generated MCQ published successfully"));
    }
}
