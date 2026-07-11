package com.ajith.codejudge.question.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "coding_questions")
@PrimaryKeyJoinColumn(name = "id")
public class CodingQuestion extends Question {

    @Column(columnDefinition = "TEXT")
    private String constraints;

    @Column(name = "time_limit_ms", nullable = false)
    @Builder.Default
    private int timeLimitMs = 1000;

    @Column(name = "memory_limit_mb", nullable = false)
    @Builder.Default
    private int memoryLimitMb = 256;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "coding_question_languages",
            joinColumns = @JoinColumn(name = "coding_question_id"),
            inverseJoinColumns = @JoinColumn(name = "language_id")
    )
    @Builder.Default
    private Set<Language> allowedLanguages = new HashSet<>();

    @OneToMany(mappedBy = "codingQuestion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TestCase> testCases = new ArrayList<>();

    public void addTestCase(TestCase testCase) {
        testCases.add(testCase);
        testCase.setCodingQuestion(this);
    }

    public void removeTestCase(TestCase testCase) {
        testCases.remove(testCase);
        testCase.setCodingQuestion(null);
    }
}
