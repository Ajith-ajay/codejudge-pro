package com.ajith.codejudge.learning.roadmap.entity;

import com.ajith.codejudge.user.entity.User;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
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
@Table(name = "learning_roadmaps")
public class LearningRoadmap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(nullable = false, length = 255)
    private String goal;

    @Column(name = "target_role", nullable = false, length = 100)
    private String targetRole;

    @Column(name = "preferred_language", nullable = false, length = 30)
    private String preferredLanguage;

    @Column(name = "duration_days", nullable = false)
    private int durationDays;

    @Column(name = "daily_minutes", nullable = false)
    private int dailyMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private LearningRoadmapStatus status = LearningRoadmapStatus.ACTIVE;

    @Column(nullable = false)
    @Builder.Default
    private int version = 1;

    @Column(name = "start_date", nullable = false)
    private java.time.LocalDate startDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "generated_payload", nullable = false, columnDefinition = "jsonb")
    private String generatedPayload;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "roadmap", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dayNumber ASC")
    @Builder.Default
    private List<LearningRoadmapDay> days = new ArrayList<>();

    public void addDay(LearningRoadmapDay day) {
        days.add(day);
        day.setRoadmap(this);
    }
}
