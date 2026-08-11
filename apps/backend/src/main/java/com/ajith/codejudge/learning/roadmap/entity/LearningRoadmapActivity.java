package com.ajith.codejudge.learning.roadmap.entity;

import com.ajith.codejudge.skill.entity.Skill;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "learning_roadmap_activities")
public class LearningRoadmapActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_id", nullable = false)
    private LearningRoadmapDay day;

    @Column(name = "sequence_no", nullable = false)
    private int sequenceNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 20)
    private RoadmapActivityType activityType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String instructions;

    @Column(name = "estimated_minutes", nullable = false)
    private int estimatedMinutes;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "learning_roadmap_activity_skills",
            joinColumns = @JoinColumn(name = "activity_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    @Builder.Default
    private Set<Skill> skills = new HashSet<>();
}
