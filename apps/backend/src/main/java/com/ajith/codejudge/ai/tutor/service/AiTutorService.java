package com.ajith.codejudge.ai.tutor.service;

import com.ajith.codejudge.ai.tutor.dto.request.TutorChatRequest;
import com.ajith.codejudge.ai.tutor.dto.response.TutorChatResponse;
import com.ajith.codejudge.ai.tutor.dto.response.TutorConversationResponse;

public interface AiTutorService {

    TutorChatResponse chat(Long userId, TutorChatRequest request);

    TutorConversationResponse getConversation(Long userId, Long conversationId);
}
