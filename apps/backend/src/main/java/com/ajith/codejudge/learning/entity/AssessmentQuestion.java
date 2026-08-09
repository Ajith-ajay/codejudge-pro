package com.ajith.codejudge.learning.entity;

import com.ajith.codejudge.question.entity.Question;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(AssessmentQuestionId.class)
@Table(name = "assessment_questions")
public class AssessmentQuestion {

    @Id
    @Column(name = "assessment_id")
    private Long assessmentId;

    @Id
    @Column(name = "question_id")
    private Long questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false, insertable = false, updatable = false)
    private LearningAssessment assessment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false, insertable = false, updatable = false)
    private Question question;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;
}
