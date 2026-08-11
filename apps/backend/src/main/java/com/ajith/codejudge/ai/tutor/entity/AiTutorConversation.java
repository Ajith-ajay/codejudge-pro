package com.ajith.codejudge.ai.tutor.entity;

import com.ajith.codejudge.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ai_tutor_conversations")
public class AiTutorConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "question_id")
    private Long questionId;

    @Column(name = "submission_id")
    private Long submissionId;

    @Column(name = "roadmap_id")
    private Long roadmapId;

    @Column(name = "skill_id")
    private Long skillId;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<AiTutorMessage> messages = new ArrayList<>();

    public void addMessage(AiTutorMessage message) {
        messages.add(message);
        message.setConversation(this);
        updatedAt = LocalDateTime.now();
    }
}
