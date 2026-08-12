package com.ajith.codejudge.learning.session.entity;

import com.ajith.codejudge.question.entity.Question;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "learning_session_activity_questions",
        uniqueConstraints = {
            @UniqueConstraint(name = "uq_learning_activity_question",
                    columnNames = {"activity_id", "question_id"}),
            @UniqueConstraint(name = "uq_learning_activity_question_order",
                    columnNames = {"activity_id", "order_index"})
        })
public class LearningSessionActivityQuestion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private LearningSessionActivity activity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;
}
