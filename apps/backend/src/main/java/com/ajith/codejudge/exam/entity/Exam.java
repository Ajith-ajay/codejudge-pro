package com.ajith.codejudge.exam.entity;

import com.ajith.codejudge.common.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@lombok.experimental.SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "exams")
public class Exam extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Column(name = "pass_percentage", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal passPercentage = BigDecimal.ZERO;

    @Column(name = "is_published", nullable = false)
    @Builder.Default
    private boolean published = false;

    @Column(name = "is_closed", nullable = false)
    @Builder.Default
    private boolean closed = false;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Section> sections = new ArrayList<>();

    public void addSection(Section section) {
        sections.add(section);
        section.setExam(this);
    }

    public void removeSection(Section section) {
        sections.remove(section);
        section.setExam(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Exam exam = (Exam) o;
        return Objects.equals(id, exam.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
