package com.ajith.codejudge.learning.entity;

import com.ajith.codejudge.skill.entity.Skill;
import com.ajith.codejudge.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_skill_progress", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "skill_id"}))
public class UserSkillProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(nullable = false)
    @Builder.Default
    private double masteryScore = 0;

    @Column(nullable = false)
    @Builder.Default
    private double confidenceScore = 0;

    @Column(nullable = false)
    @Builder.Default
    private int attempts = 0;

    @Column(nullable = false)
    @Builder.Default
    private int correctAttempts = 0;

    @Column(nullable = false)
    @Builder.Default
    private int codingAttempts = 0;

    @Column(nullable = false)
    @Builder.Default
    private int codingCorrect = 0;

    @Column(nullable = false)
    @Builder.Default
    private int mcqAttempts = 0;

    @Column(nullable = false)
    @Builder.Default
    private int mcqCorrect = 0;

    private LocalDateTime lastAttemptedAt;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
