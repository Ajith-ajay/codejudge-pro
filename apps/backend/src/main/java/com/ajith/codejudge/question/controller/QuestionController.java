package com.ajith.codejudge.question.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ajith.codejudge.common.pagination.PageRequestDto;
import com.ajith.codejudge.common.pagination.PageResponseDto;
import com.ajith.codejudge.common.response.ApiResponse;
import com.ajith.codejudge.question.dto.request.CodingQuestionRequest;
import com.ajith.codejudge.question.dto.request.LanguageRequest;
import com.ajith.codejudge.question.dto.request.McqQuestionRequest;
import com.ajith.codejudge.question.dto.request.QuestionSkillRequest;
import com.ajith.codejudge.question.dto.response.CodingQuestionResponse;
import com.ajith.codejudge.question.dto.response.LanguageResponse;
import com.ajith.codejudge.question.dto.response.McqQuestionResponse;
import com.ajith.codejudge.question.dto.response.QuestionResponse;
import com.ajith.codejudge.question.service.interfaces.QuestionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
@Tag(name = "Questions", description = "Endpoints for managing compiler languages, MCQs, and Coding Questions")
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping("/languages")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Create a new supported compiler programming language")
    public ResponseEntity<ApiResponse<LanguageResponse>> createLanguage(@Valid @RequestBody LanguageRequest request) {
        LanguageResponse response = questionService.createLanguage(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Language created successfully"));
    }

    @GetMapping("/languages")
    @Operation(summary = "Get list of all supported compiler programming languages")
    public ResponseEntity<ApiResponse<List<LanguageResponse>>> getAllLanguages() {
        List<LanguageResponse> response = questionService.getAllLanguages();
        return ResponseEntity.ok(ApiResponse.success(response, "Languages retrieved successfully"));
    }

    @PostMapping("/mcq")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EXAM_SETTER')")
    @Operation(summary = "Create a new MCQ question")
    public ResponseEntity<ApiResponse<McqQuestionResponse>> createMcq(@Valid @RequestBody McqQuestionRequest request) {
        McqQuestionResponse response = questionService.createMcqQuestion(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "MCQ Question created successfully"));
    }

    @PostMapping("/coding")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EXAM_SETTER')")
    @Operation(summary = "Create a new Coding question with testcases")
    public ResponseEntity<ApiResponse<CodingQuestionResponse>> createCoding(@Valid @RequestBody CodingQuestionRequest request) {
        CodingQuestionResponse response = questionService.createCodingQuestion(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Coding Question created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'SUPER_ADMIN', 'ADMIN', 'EXAM_SETTER')")
    @Operation(summary = "Retrieve a question by ID (Authenticated users)")
    public ResponseEntity<ApiResponse<QuestionResponse>> getQuestionById(@PathVariable Long id) {
        QuestionResponse response = questionService.getQuestionById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Question retrieved successfully"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CANDIDATE', 'SUPER_ADMIN', 'ADMIN', 'EXAM_SETTER')")
    @Operation(summary = "Retrieve a paginated list of all questions (Authenticated users)")
    public ResponseEntity<ApiResponse<PageResponseDto<QuestionResponse>>> getAllQuestions(@Valid PageRequestDto pageRequest) {
        PageResponseDto<QuestionResponse> response = questionService.getAllQuestions(pageRequest);
        return ResponseEntity.ok(ApiResponse.success(response, "Questions list retrieved successfully"));
    }

    @PutMapping("/mcq/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EXAM_SETTER')")
    @Operation(summary = "Update an existing MCQ question")
    public ResponseEntity<ApiResponse<McqQuestionResponse>> updateMcq(@PathVariable Long id, @Valid @RequestBody McqQuestionRequest request) {
        McqQuestionResponse response = questionService.updateMcqQuestion(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "MCQ Question updated successfully"));
    }

    @PutMapping("/coding/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EXAM_SETTER')")
    @Operation(summary = "Update an existing Coding question")
    public ResponseEntity<ApiResponse<CodingQuestionResponse>> updateCoding(@PathVariable Long id, @Valid @RequestBody CodingQuestionRequest request) {
        CodingQuestionResponse response = questionService.updateCodingQuestion(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Coding Question updated successfully"));
    }

    @PutMapping("/{id}/skills")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EXAM_SETTER')")
    @Operation(summary = "Assign learning skills to a question")
    public ResponseEntity<ApiResponse<Void>> assignSkills(
            @PathVariable Long id,
            @Valid @RequestBody QuestionSkillRequest request) {
        questionService.assignSkills(id, request);
        return ResponseEntity.ok(ApiResponse.success("Question skills updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EXAM_SETTER')")
    @Operation(summary = "Delete an existing question")
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.ok(ApiResponse.success("Question deleted successfully"));
    }
}
