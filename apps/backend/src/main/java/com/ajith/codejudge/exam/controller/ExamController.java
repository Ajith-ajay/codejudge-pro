package com.ajith.codejudge.exam.controller;

import com.ajith.codejudge.common.pagination.PageRequestDto;
import com.ajith.codejudge.common.pagination.PageResponseDto;
import com.ajith.codejudge.common.response.ApiResponse;
import com.ajith.codejudge.exam.dto.request.CandidateInviteRequest;
import com.ajith.codejudge.exam.dto.request.ExamRequest;
import com.ajith.codejudge.exam.dto.request.ActivityLogRequest;
import com.ajith.codejudge.exam.dto.response.ExamCandidateResponse;
import com.ajith.codejudge.exam.dto.response.ExamResponse;
import com.ajith.codejudge.exam.dto.response.LeaderboardEntryDto;
import com.ajith.codejudge.exam.dto.response.ActivityLogResponse;
import com.ajith.codejudge.exam.service.interfaces.ExamService;
import com.ajith.codejudge.exam.service.interfaces.LeaderboardService;
import com.ajith.codejudge.exam.service.interfaces.ActivityLogService;
import com.ajith.codejudge.security.service.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exams")
@RequiredArgsConstructor
@Tag(name = "Exams", description = "Endpoints for scheduling exams, managing sections, and inviting candidates")
public class ExamController {

    private final ExamService examService;
    private final LeaderboardService leaderboardService;
    private final ActivityLogService activityLogService;

    // --- Admin and Exam Setter CRUD Endpoints ---

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EXAM_SETTER')")
    @Operation(summary = "Create a new exam session with section partitions and mapped questions")
    public ResponseEntity<ApiResponse<ExamResponse>> createExam(@Valid @RequestBody ExamRequest request) {
        ExamResponse response = examService.createExam(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Exam created and structured successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EXAM_SETTER')")
    @Operation(summary = "Update structural parameters or section configurations of an exam")
    public ResponseEntity<ApiResponse<ExamResponse>> updateExam(@PathVariable Long id, @Valid @RequestBody ExamRequest request) {
        ExamResponse response = examService.updateExam(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Exam updated successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EXAM_SETTER')")
    @Operation(summary = "Get full detailed exam configuration by ID")
    public ResponseEntity<ApiResponse<ExamResponse>> getExamById(@PathVariable Long id) {
        ExamResponse response = examService.getExamById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Exam details retrieved successfully"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EXAM_SETTER')")
    @Operation(summary = "Get paginated list of all created exams")
    public ResponseEntity<ApiResponse<PageResponseDto<ExamResponse>>> getAllExams(@Valid PageRequestDto pageRequest) {
        PageResponseDto<ExamResponse> response = examService.getAllExams(pageRequest);
        return ResponseEntity.ok(ApiResponse.success(response, "Exams retrieved successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EXAM_SETTER')")
    @Operation(summary = "Delete an exam session")
    public ResponseEntity<ApiResponse<Void>> deleteExam(@PathVariable Long id) {
        examService.deleteExam(id);
        return ResponseEntity.ok(ApiResponse.success("Exam deleted successfully"));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EXAM_SETTER')")
    @Operation(summary = "Publish an exam to make it visible to invited candidates")
    public ResponseEntity<ApiResponse<ExamResponse>> publishExam(@PathVariable Long id) {
        ExamResponse response = examService.publishExam(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Exam published successfully"));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EXAM_SETTER')")
    @Operation(summary = "Close an active exam, preventing further entry attempts")
    public ResponseEntity<ApiResponse<ExamResponse>> closeExam(@PathVariable Long id) {
        ExamResponse response = examService.closeExam(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Exam closed successfully"));
    }

    @PostMapping("/{id}/candidates")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EXAM_SETTER')")
    @Operation(summary = "Invite candidate emails to join this exam, triggering notification dispatches")
    public ResponseEntity<ApiResponse<List<ExamCandidateResponse>>> inviteCandidates(
            @PathVariable Long id, @Valid @RequestBody CandidateInviteRequest request) {
        List<ExamCandidateResponse> response = examService.inviteCandidates(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Candidates invited successfully"));
    }

    @GetMapping("/{id}/candidates")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EXAM_SETTER')")
    @Operation(summary = "Get list of all candidates invited to this exam")
    public ResponseEntity<ApiResponse<List<ExamCandidateResponse>>> getExamCandidates(@PathVariable Long id) {
        List<ExamCandidateResponse> response = examService.getExamCandidates(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Exam candidates list retrieved successfully"));
    }

    // --- Candidate Endpoints ---

    @GetMapping("/candidate/active")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Retrieve active exams currently visible to the authenticated candidate")
    public ResponseEntity<ApiResponse<List<ExamResponse>>> getCandidateExams(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<ExamResponse> response = examService.getActiveExamsForCandidate(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Active candidate exams retrieved successfully"));
    }

    @PostMapping("/candidate/active/{id}/start")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Start taking an active exam session")
    public ResponseEntity<ApiResponse<ExamCandidateResponse>> startExamAttempt(
            @PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        ExamCandidateResponse response = examService.startExam(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Exam attempt started successfully"));
    }

    @PostMapping("/candidate/active/{id}/complete")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Finish and submit an active exam session")
    public ResponseEntity<ApiResponse<ExamCandidateResponse>> completeExamAttempt(
            @PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        ExamCandidateResponse response = examService.completeExam(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Exam attempt completed and submitted"));
    }

    // --- Analytics, Leaderboard & Anti-Cheating Endpoints ---

    @GetMapping("/{id}/leaderboard")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'EXAM_SETTER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Retrieve real-time Redis-backed standings or database fallbacks for an exam")
    public ResponseEntity<ApiResponse<List<LeaderboardEntryDto>>> getLeaderboard(@PathVariable Long id) {
        List<LeaderboardEntryDto> leaderboard = leaderboardService.getLeaderboard(id);
        return ResponseEntity.ok(ApiResponse.success(leaderboard, "Real-time leaderboard standings retrieved"));
    }

    @PostMapping("/candidate/active/{id}/activity")
    @PreAuthorize("hasAnyRole('CANDIDATE')")
    @Operation(summary = "Log an anti-cheating candidate browser activity telemetry event (e.g. TAB_SWITCH, BLUR)")
    public ResponseEntity<ApiResponse<ActivityLogResponse>> logCandidateActivity(
            @PathVariable("id") Long candidateId,
            @Valid @RequestBody ActivityLogRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        ActivityLogResponse response = activityLogService.logActivity(candidateId, request, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Activity log captured successfully"));
    }

    @GetMapping("/{id}/activities")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EXAM_SETTER')")
    @Operation(summary = "Get anti-cheating tracking audit logs for all candidates in an exam (Admin/Setters only)")
    public ResponseEntity<ApiResponse<List<ActivityLogResponse>>> getLogsByExam(@PathVariable Long id) {
        List<ActivityLogResponse> response = activityLogService.getLogsByExam(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Exam candidate activity logs retrieved"));
    }

    @GetMapping("/candidate/{candidateId}/activities")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EXAM_SETTER')")
    @Operation(summary = "Get anti-cheating activity logs for a specific candidate enrollment (Admin/Setters only)")
    public ResponseEntity<ApiResponse<List<ActivityLogResponse>>> getLogsByCandidate(@PathVariable Long candidateId) {
        List<ActivityLogResponse> response = activityLogService.getLogsByCandidate(candidateId);
        return ResponseEntity.ok(ApiResponse.success(response, "Candidate activity logs retrieved"));
    }
}
