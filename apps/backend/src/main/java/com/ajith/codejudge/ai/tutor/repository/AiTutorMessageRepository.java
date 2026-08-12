package com.ajith.codejudge.ai.tutor.repository;

import com.ajith.codejudge.ai.tutor.entity.AiTutorMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiTutorMessageRepository extends JpaRepository<AiTutorMessage, Long> {

    List<AiTutorMessage> findTop10ByConversationIdOrderByCreatedAtDesc(Long conversationId);
}
