package com.ajith.codejudge.ai.validator;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.ajith.codejudge.ai.dto.response.GeneratedMcq;
import com.ajith.codejudge.ai.dto.response.GeneratedMcqOption;
import com.ajith.codejudge.question.entity.Difficulty;

@Component
public class GeneratedMcqValidator {

    public void validate(GeneratedMcq mcq, Difficulty requestedDifficulty, int requestedMarks,
            boolean requestedMultipleChoice, BigDecimal requestedNegativeMarking,
            boolean requestedPartialMarking, boolean requestedRandomizeOptions) {
        if (mcq == null) {
            throw new IllegalArgumentException("Generated MCQ is empty");
        }
        if (blank(mcq.getTitle()) || mcq.getTitle().length() > 255) {
            throw new IllegalArgumentException("Generated MCQ title is invalid");
        }
        if (blank(mcq.getDescription())) {
            throw new IllegalArgumentException("Generated MCQ description is empty");
        }
        if (mcq.getDifficulty() != requestedDifficulty) {
            throw new IllegalArgumentException("Generated difficulty does not match requested difficulty");
        }
        if (mcq.getMarks() != requestedMarks) {
            throw new IllegalArgumentException("Generated marks do not match requested marks");
        }
        if (mcq.getOptions() == null || mcq.getOptions().size() != 4) {
            throw new IllegalArgumentException("Generated MCQ must contain exactly 4 options");
        }
        if (mcq.isMultipleChoice() != requestedMultipleChoice) {
            throw new IllegalArgumentException("Generated multiple-choice setting does not match request");
        }
        if (mcq.getNegativeMarking() == null || mcq.getNegativeMarking().signum() < 0) {
            throw new IllegalArgumentException("Generated negative marking is invalid");
        }
        if (requestedNegativeMarking == null || mcq.getNegativeMarking().compareTo(requestedNegativeMarking) != 0) {
            throw new IllegalArgumentException("Generated negative marking does not match request");
        }
        if (mcq.isPartialMarking() != requestedPartialMarking) {
            throw new IllegalArgumentException("Generated partial-marking setting does not match request");
        }
        if (mcq.isRandomizeOptions() != requestedRandomizeOptions) {
            throw new IllegalArgumentException("Generated randomize-options setting does not match request");
        }
        if (blank(mcq.getExplanation())) {
            throw new IllegalArgumentException("Generated explanation is empty");
        }

        Set<String> ids = new HashSet<>();
        Set<String> texts = new HashSet<>();
        int correct = 0;
        for (GeneratedMcqOption option : mcq.getOptions()) {
            if (option == null || blank(option.getId()) || blank(option.getText())) {
                throw new IllegalArgumentException("Every MCQ option must contain an id and text");
            }
            if (!ids.add(option.getId())) {
                throw new IllegalArgumentException("Duplicate MCQ option id");
            }
            if (!texts.add(option.getText().trim().toLowerCase())) {
                throw new IllegalArgumentException("Duplicate MCQ option text");
            }
            if (option.isCorrect()) {
                correct++;
            }
        }

        if (requestedMultipleChoice) {
            if (correct < 2 || correct > 3) {
                throw new IllegalArgumentException("Multiple-choice MCQ must contain 2 or 3 correct options");
            }
        } else if (correct != 1) {
            throw new IllegalArgumentException("Single-answer MCQ must contain exactly one correct option");
        }
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
