package com.ajith.codejudge.learning.entity;

import com.ajith.codejudge.skill.entity.Skill;
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
@Table(name = "learning_assessments")
public class LearningAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id")
    private Skill skill;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private LearningAssessmentStatus status = LearningAssessmentStatus.GENERATED;

    @Column(name = "total_questions", nullable = false)
    private int totalQuestions;

    @Column(name = "mcq_count", nullable = false)
    private int mcqCount;

    @Column(name = "coding_count", nullable = false)
    private int codingCount;

    @Column(name = "target_difficulty", nullable = false, length = 20)
    private String targetDifficulty;

    private Double score;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<AssessmentQuestion> questions = new ArrayList<>();

    public void addQuestion(AssessmentQuestion question) {
        questions.add(question);
        question.setAssessment(this);
    }
}
