package com.ajith.codejudge.ai.tutor.repository;

import com.ajith.codejudge.ai.tutor.entity.AiTutorConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AiTutorConversationRepository extends JpaRepository<AiTutorConversation, Long> {

    @Query("""
        select c
        from AiTutorConversation c
        where c.id = :id and c.user.id = :userId
        """)
    Optional<AiTutorConversation> findOwned(
            @Param("id") Long id,
            @Param("userId") Long userId);
}
