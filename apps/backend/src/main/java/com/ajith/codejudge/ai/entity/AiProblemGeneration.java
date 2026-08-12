package com.ajith.codejudge.ai.entity;

import com.ajith.codejudge.question.entity.Difficulty;
import com.ajith.codejudge.question.entity.Question;
import com.ajith.codejudge.skill.entity.Skill;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ai_problem_generations")
public class AiProblemGeneration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Difficulty difficulty;

    @Column(name = "language_code", nullable = false, length = 20)
    private String languageCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AiProblemGenerationStatus status = AiProblemGenerationStatus.GENERATING;

    @Column(name = "generated_payload", columnDefinition = "TEXT")
    private String generatedPayload;

    @Column(name = "validation_message", columnDefinition = "TEXT")
    private String validationMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "validated_at")
    private LocalDateTime validatedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;
}
