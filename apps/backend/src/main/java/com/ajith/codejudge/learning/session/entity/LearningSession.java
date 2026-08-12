package com.ajith.codejudge.learning.session.entity;

import com.ajith.codejudge.learning.roadmap.entity.LearningRoadmap;
import com.ajith.codejudge.learning.roadmap.entity.LearningRoadmapDay;
import com.ajith.codejudge.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "learning_sessions",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_learning_session_user_date",
                columnNames = {"user_id", "session_date"}
        )
)
public class LearningSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roadmap_id", nullable = false)
    private LearningRoadmap roadmap;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roadmap_day_id", nullable = false)
    private LearningRoadmapDay roadmapDay;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private LearningSessionStatus status = LearningSessionStatus.NOT_STARTED;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceNo ASC")
    @Builder.Default
    private List<LearningSessionActivity> activities = new ArrayList<>();

    public void addActivity(LearningSessionActivity activity) {
        activities.add(activity);
        activity.setSession(this);
    }
}
