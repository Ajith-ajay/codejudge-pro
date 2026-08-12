package com.ajith.codejudge.learning.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentQuestionId implements Serializable {
    private Long assessmentId;
    private Long questionId;
}
