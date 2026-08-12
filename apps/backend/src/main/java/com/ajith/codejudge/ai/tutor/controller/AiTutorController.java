package com.ajith.codejudge.ai.tutor.controller;

import com.ajith.codejudge.ai.tutor.dto.request.TutorChatRequest;
import com.ajith.codejudge.ai.tutor.dto.response.TutorChatResponse;
import com.ajith.codejudge.ai.tutor.dto.response.TutorConversationResponse;
import com.ajith.codejudge.ai.tutor.service.AiTutorService;
import com.ajith.codejudge.common.response.ApiResponse;
import com.ajith.codejudge.security.service.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai/tutor")
@RequiredArgsConstructor
@Tag(name = "AI Tutor", description = "Personalized AI teaching and debugging assistant")
public class AiTutorController {

    private final AiTutorService tutorService;

    @PostMapping("/chat")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'EXAM_SETTER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Chat with the personalized CodeJudgePro AI tutor")
    public ResponseEntity<ApiResponse<TutorChatResponse>> chat(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody TutorChatRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                tutorService.chat(userDetails.getId(), request),
                "Tutor response generated successfully"
        ));
    }

    @GetMapping("/conversations/{conversationId}")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'EXAM_SETTER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get a user's AI tutor conversation")
    public ResponseEntity<ApiResponse<TutorConversationResponse>> getConversation(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long conversationId) {

        return ResponseEntity.ok(ApiResponse.success(
                tutorService.getConversation(userDetails.getId(), conversationId),
                "Tutor conversation retrieved successfully"
        ));
    }
}
