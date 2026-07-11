package com.ajith.codejudge.submission.controller;

import com.ajith.codejudge.common.response.ApiResponse;
import com.ajith.codejudge.security.service.UserDetailsImpl;
import com.ajith.codejudge.submission.dto.request.SubmissionRequest;
import com.ajith.codejudge.submission.dto.response.SubmissionResponse;
import com.ajith.codejudge.submission.service.interfaces.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/submissions")
@RequiredArgsConstructor
@Tag(name = "Submissions", description = "Endpoints for submitting solutions and retrieving execution reports")
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CANDIDATE', 'EXAM_SETTER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Submit a coding or MCQ question solution for grading")
    public ResponseEntity<ApiResponse<SubmissionResponse>> submitSolution(
            @Valid @RequestBody SubmissionRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        SubmissionResponse response = submissionService.submitSolution(request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Solution submitted and graded successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'EXAM_SETTER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Retrieve grading details for a specific submission (Security rules enforced)")
    public ResponseEntity<ApiResponse<SubmissionResponse>> getSubmissionById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        SubmissionResponse response = submissionService.getSubmissionById(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Submission details retrieved"));
    }

    @GetMapping("/candidate/{candidateId}")
    @PreAuthorize("hasAnyRole('EXAM_SETTER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get list of all submissions for a candidate (Admins/Exam Setters only)")
    public ResponseEntity<ApiResponse<List<SubmissionResponse>>> getSubmissionsByCandidate(@PathVariable Long candidateId) {
        List<SubmissionResponse> response = submissionService.getSubmissionsByCandidate(candidateId);
        return ResponseEntity.ok(ApiResponse.success(response, "Candidate submissions retrieved"));
    }

    @GetMapping("/question/{questionId}")
    @PreAuthorize("hasAnyRole('EXAM_SETTER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get list of all submissions for a question (Admins/Exam Setters only)")
    public ResponseEntity<ApiResponse<List<SubmissionResponse>>> getSubmissionsByQuestion(@PathVariable Long questionId) {
        List<SubmissionResponse> response = submissionService.getSubmissionsByQuestion(questionId);
        return ResponseEntity.ok(ApiResponse.success(response, "Question submissions retrieved"));
    }
}
