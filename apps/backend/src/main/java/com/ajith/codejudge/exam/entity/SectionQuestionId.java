package com.ajith.codejudge.exam.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionQuestionId implements Serializable {

    private Long section;
    private Long question;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SectionQuestionId that = (SectionQuestionId) o;
        return Objects.equals(section, that.section) && Objects.equals(question, that.question);
    }

    @Override
    public int hashCode() {
        return Objects.hash(section, question);
    }
}
