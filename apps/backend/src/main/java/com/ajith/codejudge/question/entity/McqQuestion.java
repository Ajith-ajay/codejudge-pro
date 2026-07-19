package com.ajith.codejudge.question.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mcq_questions")
@PrimaryKeyJoinColumn(name = "id")
public class McqQuestion extends Question {

    @Convert(converter = McqOptionConverter.class)
    @Column(nullable = false, columnDefinition = "jsonb")
    @lombok.Builder.Default
    private List<McqOption> options = new ArrayList<>();

    @Column(name = "is_multiple_choice", nullable = false)
    private boolean isMultipleChoice;

    @Column(name = "negative_marking", nullable = false, precision = 5, scale = 2)
    private BigDecimal negativeMarking;

    @Column(name = "partial_marking", nullable = false)
    private boolean partialMarking;

    @Column(name = "randomize_options", nullable = false)
    private boolean randomizeOptions;
}
