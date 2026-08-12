package com.ajith.codejudge.ai.tutor.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class TutorChatResponse {
    private Long conversationId;
    private String reply;
    private String diagnosis;
    private List<String> nextSteps;
    private String practiceSuggestion;
    private LocalDateTime createdAt;
}
