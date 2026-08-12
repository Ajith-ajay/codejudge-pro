package com.ajith.codejudge.ai.tutor.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorChatRequest {

    private Long conversationId;

    @NotBlank(message = "Message is required")
    @Size(max = 6000, message = "Message must not exceed 6000 characters")
    private String message;

    private Long questionId;
    private Long submissionId;
    private Long roadmapId;
    private Long skillId;
}
