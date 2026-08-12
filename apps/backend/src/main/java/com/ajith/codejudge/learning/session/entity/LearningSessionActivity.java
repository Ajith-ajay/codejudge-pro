package com.ajith.codejudge.learning.session.entity;

import com.ajith.codejudge.learning.roadmap.entity.LearningRoadmapActivity;
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
@Table(name = "learning_session_activities",
        uniqueConstraints = {
            @UniqueConstraint(name = "uq_learning_session_activity_sequence",
                    columnNames = {"session_id", "sequence_no"}),
            @UniqueConstraint(name = "uq_learning_session_roadmap_activity",
                    columnNames = {"session_id", "roadmap_activity_id"})
        })
public class LearningSessionActivity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private LearningSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roadmap_activity_id", nullable = false)
    private LearningRoadmapActivity roadmapActivity;

    @Column(name = "sequence_no", nullable = false)
    private int sequenceNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private LearningSessionActivityStatus status = LearningSessionActivityStatus.NOT_STARTED;

    private Double score;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<LearningSessionActivityQuestion> questions = new ArrayList<>();

    public void addQuestion(LearningSessionActivityQuestion question) {
        questions.add(question);
        question.setActivity(this);
    }
}
