package com.ajith.codejudge.ai.service;

import java.time.LocalDateTime;
import java.util.HashSet;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ajith.codejudge.ai.client.LlmClient;
import com.ajith.codejudge.ai.dto.request.GenerateProblemRequest;
import com.ajith.codejudge.ai.dto.response.GeneratedProblem;
import com.ajith.codejudge.ai.dto.response.GeneratedTestCase;
import com.ajith.codejudge.ai.dto.response.ProblemGenerationResponse;
import com.ajith.codejudge.ai.entity.AiProblemGeneration;
import com.ajith.codejudge.ai.entity.AiProblemGenerationStatus;
import com.ajith.codejudge.ai.repository.AiProblemGenerationRepository;
import com.ajith.codejudge.ai.validator.GeneratedProblemValidator;
import com.ajith.codejudge.compiler.sandbox.DockerSandboxExecutor;
import com.ajith.codejudge.compiler.sandbox.ExecutionResult;
import com.ajith.codejudge.exception.AiServiceException;
import com.ajith.codejudge.exception.BadRequestException;
import com.ajith.codejudge.exception.ConflictException;
import com.ajith.codejudge.exception.ResourceNotFoundException;
import com.ajith.codejudge.question.dto.response.CodingQuestionResponse;
import com.ajith.codejudge.question.entity.CodingQuestion;
import com.ajith.codejudge.question.entity.Language;
import com.ajith.codejudge.question.entity.TestCase;
import com.ajith.codejudge.question.mapper.QuestionMapper;
import com.ajith.codejudge.question.repository.LanguageRepository;
import com.ajith.codejudge.question.repository.QuestionRepository;
import com.ajith.codejudge.skill.entity.Skill;
import com.ajith.codejudge.skill.repository.SkillRepository;
import com.ajith.codejudge.submission.entity.SubmissionStatus;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemGenerationServiceImpl implements ProblemGenerationService {

    private final SkillRepository skillRepository;
    private final LanguageRepository languageRepository;
    private final QuestionRepository questionRepository;
    private final AiProblemGenerationRepository generationRepository;
    private final LlmClient llmClient;
    private final GeneratedProblemValidator validator;
    private final DockerSandboxExecutor sandboxExecutor;
    private final QuestionMapper questionMapper;
    private final ObjectMapper objectMapper;

    @Override
    public ProblemGenerationResponse generate(GenerateProblemRequest request) {
        Skill skill = skillRepository.findById(request.getSkillId())
                .filter(Skill::isActive)
                .orElseThrow(() -> new ResourceNotFoundException(
                "Active skill not found with id: " + request.getSkillId()));

        String languageCode = request.getLanguageCode().trim().toLowerCase();
        Language language = languageRepository.findByCode(languageCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                "Supported language not found: " + languageCode));

        AiProblemGeneration generation = generationRepository.save(
                AiProblemGeneration.builder()
                        .skill(skill)
                        .difficulty(request.getDifficulty())
                        .languageCode(language.getCode())
                        .status(AiProblemGenerationStatus.GENERATING)
                        .build()
        );

        try {
            String rawJson = llmClient.generateProblem(buildPrompt(skill, request, language));
            GeneratedProblem problem = objectMapper.readValue(rawJson, GeneratedProblem.class);

            validator.validate(problem, request.getDifficulty(), languageCode);

            generateExpectedOutputs(problem, languageCode);

            // validateReferenceSolution(problem, languageCode);
            generation.setGeneratedPayload(objectMapper.writeValueAsString(problem));
            generation.setStatus(AiProblemGenerationStatus.VALIDATED);
            generation.setValidatedAt(LocalDateTime.now());
            generation.setValidationMessage("Problem generated and reference solution passed all test cases");
            generationRepository.save(generation);

            return toResponse(generation, problem);
        } catch (Exception ex) {
            generation.setStatus(AiProblemGenerationStatus.FAILED);
            generation.setValidationMessage(ex.getMessage() == null ? "Generation validation failed" : ex.getMessage());
            generationRepository.save(generation);
            log.error("AI problem generation failed for generationId={}", generation.getId(), ex);
            if (ex instanceof AiServiceException) {
                throw (AiServiceException) ex;
            }
            if (ex instanceof BadRequestException) {
                throw (BadRequestException) ex;
            }
            throw new BadRequestException("Generated problem failed validation: " + generation.getValidationMessage());
        }
    }

    private void generateExpectedOutputs(GeneratedProblem problem, String languageCode) {

        if (problem.getTestCases() == null
                || problem.getTestCases().isEmpty()) {
            throw new IllegalArgumentException(
                    "Generated problem contains no test cases"
            );
        }

        if (problem.getReferenceSolution() == null
                || problem.getReferenceSolution().isBlank()) {
            throw new IllegalArgumentException(
                    "Generated problem contains no reference solution"
            );
        }

        for (GeneratedTestCase testCase : problem.getTestCases()) {

            ExecutionResult result = sandboxExecutor.executeCode(
                    languageCode,
                    problem.getReferenceSolution(),
                    testCase.getInput(),
                    problem.getTimeLimitMs(),
                    problem.getMemoryLimitMb()
            );

            if (result.getStatus() != SubmissionStatus.ACCEPTED) {
                throw new IllegalArgumentException(
                        "Reference solution failed for generated test case: "
                        + result.getStatus()
                );
            }

            String output = result.getOutput();

            if (output == null) {
                output = "";
            }

            testCase.setExpectedOutput(output.trim());
        }
    }

    private void validateReferenceSolution(GeneratedProblem problem, String languageCode) {
        for (GeneratedTestCase testCase : problem.getTestCases()) {
            ExecutionResult result = sandboxExecutor.executeCode(
                    languageCode,
                    problem.getReferenceSolution(),
                    testCase.getInput(),
                    problem.getTimeLimitMs(),
                    problem.getMemoryLimitMb()
            );

            if (result.getStatus() != com.ajith.codejudge.submission.entity.SubmissionStatus.ACCEPTED) {
                throw new IllegalArgumentException(
                        "Reference solution failed for generated test case: "
                        + (result.getErrorMessage() == null ? result.getStatus() : result.getErrorMessage())
                );
            }

            if (!normalizeOutput(testCase.getExpectedOutput()).equals(normalizeOutput(result.getOutput()))) {
                log.error("========== REFERENCE SOLUTION MISMATCH ==========");
                log.error("Test case input:\n{}", testCase.getInput());
                log.error("Expected output:\n{}", testCase.getExpectedOutput());
                log.error("Actual output:\n{}", result.getOutput());
                log.error("Reference solution:\n{}", problem.getReferenceSolution());
                log.error("=================================================");

                throw new IllegalArgumentException(
                        "Reference solution output does not match expected output for a generated test case"
                );
            }
        }
    }

    private String normalizeOutput(String output) {
        if (output == null) {
            return "";
        }
        return output.replace("\r\n", "\n").trim();
    }

    @Override
    @Transactional
    public CodingQuestionResponse publish(Long generationId) {
        AiProblemGeneration generation = generationRepository.findById(generationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                "AI problem generation not found with id: " + generationId));

        if (generation.getStatus() == AiProblemGenerationStatus.PUBLISHED) {
            throw new ConflictException("AI problem generation is already published");
        }
        if (generation.getStatus() != AiProblemGenerationStatus.VALIDATED) {
            throw new ConflictException("Only a validated AI problem can be published");
        }
        if (generation.getGeneratedPayload() == null || generation.getGeneratedPayload().isBlank()) {
            throw new ConflictException("Generated problem payload is missing");
        }

        try {
            GeneratedProblem generated = objectMapper.readValue(
                    generation.getGeneratedPayload(),
                    GeneratedProblem.class
            );

            Language language = languageRepository.findByCode(generation.getLanguageCode())
                    .orElseThrow(() -> new ResourceNotFoundException(
                    "Language not found: " + generation.getLanguageCode()));

            CodingQuestion question = CodingQuestion.builder()
                    .title(generated.getTitle())
                    .description(generated.getDescription())
                    .difficulty(generated.getDifficulty())
                    .marks(1)
                    .type(com.ajith.codejudge.question.entity.QuestionType.CODING)
                    .constraints(generated.getConstraints())
                    .timeLimitMs(generated.getTimeLimitMs())
                    .memoryLimitMb(generated.getMemoryLimitMb())
                    .allowedLanguages(new HashSet<>())
                    .build();

            question.getAllowedLanguages().add(language);
            question.getSkills().add(generation.getSkill());

            for (GeneratedTestCase generatedTestCase : generated.getTestCases()) {
                TestCase testCase = TestCase.builder()
                        .input(generatedTestCase.getInput())
                        .expectedOutput(generatedTestCase.getExpectedOutput())
                        .hidden(generatedTestCase.isHidden())
                        .marks(generatedTestCase.getMarks())
                        .build();
                question.addTestCase(testCase);
            }

            CodingQuestion saved = (CodingQuestion) questionRepository.save(question);

            generation.setQuestion(saved);
            generation.setStatus(AiProblemGenerationStatus.PUBLISHED);
            generation.setPublishedAt(LocalDateTime.now());
            generation.setValidationMessage("Published successfully");
            generationRepository.save(generation);

            return questionMapper.toResponse(saved);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BadRequestException("Failed to publish generated problem: " + ex.getMessage());
        }
    }

    private String buildPrompt(Skill skill, GenerateProblemRequest request, Language language) {
        return """
                You are the problem author for CodeJudgePro, an online judge used for placement preparation.

                Generate exactly ONE original coding problem.

                TARGET SKILL:
                %s
                CATEGORY:
                %s
                DIFFICULTY:
                %s
                PROGRAMMING LANGUAGE FOR REFERENCE SOLUTION:
                %s

                HARD REQUIREMENTS:
                1. The problem must genuinely test the target skill.
                2. It must be solvable using standard competitive-programming techniques.
                3. Input and output must use stdin/stdout only.
                4. Generate at least 4 and at most 10 meaningful test cases.
                5. Include edge cases, normal cases, and at least one hidden test case.
                6. Every expected output must be produced by the reference solution.
                7. The reference solution must be a complete executable program.
                8. For Java, use public class Solution with no package declaration.
                9. For Python, provide a complete Python 3.11 program.
                10. For C++, provide a complete C++17-compatible program.
                11. Do not use network access, files, environment variables, or external libraries.
                12. Do not include markdown fences in referenceSolution.
                13. Do not invent unsupported language-specific APIs.
                14. Keep timeLimitMs between 500 and 3000 and memoryLimitMb between 64 and 512.
                15. Test cases must be distinct.
                16. Do not copy a known problem verbatim; create a materially original variant.
                
                TEST CASE REQUIREMENTS:

                    - Generate between 4 and 10 test cases.
                    - Each test case must contain:
                    - input
                    - hidden
                    - marks
                    - Do NOT include expectedOutput.
                    - The backend will calculate expectedOutput by executing the reference solution.
                    - At least one test case must have hidden=true.
                    - Generate both visible and hidden test cases.
                    - Include normal cases, edge cases, and boundary cases.
                    - Every test input must follow the exact input format described in the problem.
                    - The reference solution must correctly solve the problem for every generated test input.

                IMPORTANT:
                    The reference solution is the authoritative implementation.
                    Do not generate expected outputs because the backend will calculate them.
                
                The final output must be only the JSON object required by the supplied schema.
                """.formatted(
                skill.getName(),
                skill.getCategory(),
                request.getDifficulty().name(),
                language.getCode()
        );
    }

    private ProblemGenerationResponse toResponse(
            AiProblemGeneration generation,
            GeneratedProblem problem
    ) {
        return ProblemGenerationResponse.builder()
                .generationId(generation.getId())
                .status(generation.getStatus())
                .skill(generation.getSkill().getName())
                .difficulty(generation.getDifficulty())
                .languageCode(generation.getLanguageCode())
                .title(problem.getTitle())
                .description(problem.getDescription())
                .constraints(problem.getConstraints())
                .timeLimitMs(problem.getTimeLimitMs())
                .memoryLimitMb(problem.getMemoryLimitMb())
                .testCaseCount(problem.getTestCases().size())
                .validationMessage(generation.getValidationMessage())
                .build();
    }
}
