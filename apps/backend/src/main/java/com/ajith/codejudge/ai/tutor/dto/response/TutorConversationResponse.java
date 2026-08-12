package com.ajith.codejudge.ai.tutor.dto.response;

import com.ajith.codejudge.ai.tutor.entity.AiTutorMessageRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class TutorConversationResponse {
    private Long id;
    private String title;
    private Long questionId;
    private Long submissionId;
    private Long roadmapId;
    private Long skillId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<MessageResponse> messages;

    @Getter
    @Builder
    public static class MessageResponse {
        private Long id;
        private AiTutorMessageRole role;
        private String content;
        private LocalDateTime createdAt;
    }
}
