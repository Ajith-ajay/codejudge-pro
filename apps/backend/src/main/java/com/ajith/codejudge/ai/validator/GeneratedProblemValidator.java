package com.ajith.codejudge.ai.validator;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.ajith.codejudge.ai.dto.response.GeneratedProblem;
import com.ajith.codejudge.ai.dto.response.GeneratedTestCase;
import com.ajith.codejudge.question.entity.Difficulty;

@Component
public class GeneratedProblemValidator {

    public void validate(GeneratedProblem problem, Difficulty requestedDifficulty, String languageCode) {
        if (problem == null) {
            throw new IllegalArgumentException("Generated problem is empty");
        }
        if (blank(problem.getTitle()) || problem.getTitle().length() > 255) {
            throw new IllegalArgumentException("Generated title is invalid");
        }
        if (blank(problem.getDescription())) {
            throw new IllegalArgumentException("Generated description is empty");
        }
        if (blank(problem.getConstraints())) {
            throw new IllegalArgumentException("Generated constraints are empty");
        }
        if (problem.getDifficulty() != requestedDifficulty) {
            throw new IllegalArgumentException("Generated difficulty does not match requested difficulty");
        }
        if (problem.getTimeLimitMs() < 100 || problem.getTimeLimitMs() > 10000) {
            throw new IllegalArgumentException("Generated time limit must be between 100ms and 10000ms");
        }
        if (problem.getMemoryLimitMb() < 16 || problem.getMemoryLimitMb() > 1024) {
            throw new IllegalArgumentException("Generated memory limit must be between 16MB and 1024MB");
        }
        if (blank(problem.getReferenceSolution())) {
            throw new IllegalArgumentException("Generated reference solution is empty");
        }

        String normalizedLanguage = languageCode.toLowerCase();
        if (!Set.of("java", "python", "cpp").contains(normalizedLanguage)) {
            throw new IllegalArgumentException("AI problem generation currently supports java, python and cpp");
        }

        if (problem.getTestCases() == null || problem.getTestCases().size() < 4 || problem.getTestCases().size() > 20) {
            throw new IllegalArgumentException("Generated problem must contain between 4 and 20 test cases");
        }

        boolean hasHidden = false;
        Set<String> inputs = new HashSet<>();
        for (GeneratedTestCase testCase : problem.getTestCases()) {
            if (blank(testCase.getInput())) {
                throw new IllegalArgumentException("Every test case must contain input and expected output");
            }
            if (testCase.getMarks() < 0) {
                throw new IllegalArgumentException("Test case marks cannot be negative");
            }
            if (!inputs.add(testCase.getInput())) {
                throw new IllegalArgumentException("Generated test cases contain duplicate inputs");
            }
            hasHidden |= testCase.isHidden();
        }

        if (!hasHidden) {
            throw new IllegalArgumentException("At least one generated test case must be hidden");
        }
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
