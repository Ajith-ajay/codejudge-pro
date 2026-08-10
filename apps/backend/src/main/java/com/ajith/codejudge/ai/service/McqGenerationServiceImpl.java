package com.ajith.codejudge.ai.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ajith.codejudge.ai.client.LlmClient;
import com.ajith.codejudge.ai.dto.request.GenerateMcqRequest;
import com.ajith.codejudge.ai.dto.response.GeneratedMcq;
import com.ajith.codejudge.ai.dto.response.GeneratedMcqOption;
import com.ajith.codejudge.ai.dto.response.McqGenerationResponse;
import com.ajith.codejudge.ai.entity.AiMcqGeneration;
import com.ajith.codejudge.ai.entity.AiMcqGenerationStatus;
import com.ajith.codejudge.ai.repository.AiMcqGenerationRepository;
import com.ajith.codejudge.ai.validator.GeneratedMcqValidator;
import com.ajith.codejudge.exception.AiServiceException;
import com.ajith.codejudge.exception.BadRequestException;
import com.ajith.codejudge.exception.ConflictException;
import com.ajith.codejudge.exception.ResourceNotFoundException;
import com.ajith.codejudge.question.dto.response.McqQuestionResponse;
import com.ajith.codejudge.question.entity.McqOption;
import com.ajith.codejudge.question.entity.McqQuestion;
import com.ajith.codejudge.question.entity.QuestionType;
import com.ajith.codejudge.question.repository.McqQuestionRepository;
import com.ajith.codejudge.skill.entity.Skill;
import com.ajith.codejudge.skill.repository.SkillRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class McqGenerationServiceImpl implements McqGenerationService {

    private final SkillRepository skillRepository;
    private final AiMcqGenerationRepository generationRepository;
    private final McqQuestionRepository mcqQuestionRepository;
    private final LlmClient llmClient;
    private final GeneratedMcqValidator validator;
    private final ObjectMapper objectMapper;

    @Override
    public McqGenerationResponse generate(GenerateMcqRequest request) {
        Skill skill = skillRepository.findById(request.getSkillId())
                .filter(Skill::isActive)
                .orElseThrow(() -> new ResourceNotFoundException(
                "Active skill not found with id: " + request.getSkillId()));

        AiMcqGeneration generation = generationRepository.save(
                AiMcqGeneration.builder()
                        .skill(skill)
                        .difficulty(request.getDifficulty())
                        .status(AiMcqGenerationStatus.GENERATING)
                        .build());

        try {
            String rawJson = llmClient.generateMcq(buildPrompt(skill, request));
            log.info("Raw MCQ JSON from LLM:\n{}", rawJson);
            GeneratedMcq mcq = objectMapper.readValue(rawJson, GeneratedMcq.class);

            log.info(
                    "Parsed MCQ: title='{}', description='{}', difficulty={}, marks={}, options={}",
                    mcq.getTitle(),
                    mcq.getDescription(),
                    mcq.getDifficulty(),
                    mcq.getMarks(),
                    mcq.getOptions()
            );

            validator.validate(
                    mcq,
                    request.getDifficulty(),
                    request.getMarks(),
                    request.isMultipleChoice(),
                    request.getNegativeMarking(),
                    request.isPartialMarking(),
                    request.isRandomizeOptions());

            generation.setGeneratedPayload(objectMapper.writeValueAsString(mcq));
            generation.setStatus(AiMcqGenerationStatus.VALIDATED);
            generation.setValidatedAt(LocalDateTime.now());
            generation.setValidationMessage("MCQ generated and validated successfully");
            generationRepository.save(generation);

            return toResponse(generation, mcq);
        } catch (Exception ex) {
            generation.setStatus(AiMcqGenerationStatus.FAILED);
            generation.setValidationMessage(
                    ex.getMessage() == null ? "MCQ generation validation failed" : ex.getMessage());
            generationRepository.save(generation);
            log.error("AI MCQ generation failed for generationId={}", generation.getId(), ex);

            if (ex instanceof AiServiceException aiServiceException) {
                throw aiServiceException;
            }
            if (ex instanceof BadRequestException badRequestException) {
                throw badRequestException;
            }
            throw new BadRequestException(
                    "Generated MCQ failed validation: " + generation.getValidationMessage());
        }
    }

    @Override
    @Transactional
    public McqQuestionResponse publish(Long generationId) {
        AiMcqGeneration generation = generationRepository.findById(generationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                "AI MCQ generation not found with id: " + generationId));

        if (generation.getStatus() == AiMcqGenerationStatus.PUBLISHED) {
            throw new ConflictException("AI MCQ generation is already published");
        }
        if (generation.getStatus() != AiMcqGenerationStatus.VALIDATED) {
            throw new ConflictException(
                    "Only a VALIDATED AI MCQ can be published. Current status: "
                    + generation.getStatus());
        }
        if (generation.getQuestion() != null) {
            throw new ConflictException("AI MCQ generation is already linked to a question");
        }

        try {
            GeneratedMcq mcq = objectMapper.readValue(
                    generation.getGeneratedPayload(), GeneratedMcq.class);

            validator.validate(
                    mcq,
                    generation.getDifficulty(),
                    mcq.getMarks(),
                    mcq.isMultipleChoice(),
                    mcq.getNegativeMarking(),
                    mcq.isPartialMarking(),
                    mcq.isRandomizeOptions());

            McqQuestion question = new McqQuestion();
            question.setTitle(mcq.getTitle().trim());
            question.setDescription(mcq.getDescription().trim());
            question.setDifficulty(mcq.getDifficulty());
            question.setMarks(mcq.getMarks());
            question.setType(QuestionType.MCQ);
            question.setSkills(new HashSet<>(List.of(generation.getSkill())));
            question.setOptions(toEntityOptions(mcq.getOptions()));
            question.setMultipleChoice(mcq.isMultipleChoice());
            question.setNegativeMarking(mcq.getNegativeMarking());
            question.setPartialMarking(mcq.isPartialMarking());
            question.setRandomizeOptions(mcq.isRandomizeOptions());
            question.setExplanation(mcq.getExplanation().trim());

            McqQuestion saved = mcqQuestionRepository.save(question);

            generation.setQuestion(saved);
            generation.setStatus(AiMcqGenerationStatus.PUBLISHED);
            generation.setPublishedAt(LocalDateTime.now());
            generation.setValidationMessage("AI-generated MCQ published successfully");
            generationRepository.save(generation);

            log.info("Published AI MCQ generationId={} questionId={}",
                    generationId, saved.getId());

            return toResponse(saved);
        } catch (ConflictException | ResourceNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to publish AI MCQ generationId={}", generationId, ex);
            throw new BadRequestException("Failed to publish AI-generated MCQ: " + ex.getMessage());
        }
    }

    private String buildPrompt(Skill skill, GenerateMcqRequest request) {
        return """
                You are an expert placement-preparation question author for CodeJudgePro.

                Generate exactly ONE high-quality multiple-choice question.

                Required skill: %s
                Skill category: %s
                Required difficulty: %s
                Marks: %d
                Multiple correct answers allowed: %s
                Negative marking: %.2f
                Partial marking: %s
                Randomize options: %s

                Requirements:
                1. The question must genuinely test the requested skill.
                2. It must be suitable for software-placement preparation.
                3. Avoid trivia and ambiguous wording.
                4. Generate exactly four distinct options.
                5. For a single-answer MCQ, exactly one option must be correct.
                6. For a multiple-answer MCQ, exactly two or three options must be correct.
                7. Include a concise but technically accurate explanation.
                8. Do not mention that the question was generated by AI.
                9. Match the requested difficulty exactly.
                10. Every required field must be present.
                11. title must be a non-empty string.
                12. description must be a non-empty string.
                13. explanation must be a non-empty string.
                14. Return ONLY valid JSON.
                15. Do not use markdown or ```json fences.
                16. Do not add any fields outside the structure below.

                Return exactly this JSON structure:

                {
                "title": "A concise question title",
                "description": "A clear description of the question",
                "difficulty": "%s",
                "marks": %d,
                "options": [
                    {
                    "id": "A",
                    "text": "First option",
                    "correct": false
                    },
                    {
                    "id": "B",
                    "text": "Second option",
                    "correct": false
                    },
                    {
                    "id": "C",
                    "text": "Third option",
                    "correct": true
                    },
                    {
                    "id": "D",
                    "text": "Fourth option",
                    "correct": false
                    }
                ],
                "multipleChoice": %s,
                "negativeMarking": %.2f,
                "partialMarking": %s,
                "randomizeOptions": %s,
                "explanation": "Explain why the correct answer is correct."
                }
                """.formatted(
                skill.getName(),
                skill.getCategory(),
                request.getDifficulty(),
                request.getMarks(),
                request.isMultipleChoice(),
                request.getNegativeMarking(),
                request.isPartialMarking(),
                request.isRandomizeOptions(),
                request.getDifficulty(),
                request.getMarks(),
                request.isMultipleChoice(),
                request.getNegativeMarking(),
                request.isPartialMarking(),
                request.isRandomizeOptions()
        );
    }

    private List<McqOption> toEntityOptions(List<GeneratedMcqOption> options) {
        List<McqOption> result = new ArrayList<>();
        for (GeneratedMcqOption option : options) {
            result.add(McqOption.builder()
                    .id(option.getId().trim())
                    .text(option.getText().trim())
                    .correct(option.isCorrect())
                    .build());
        }
        return result;
    }

    private McqGenerationResponse toResponse(
            AiMcqGeneration generation, GeneratedMcq mcq) {
        return McqGenerationResponse.builder()
                .generationId(generation.getId())
                .status(generation.getStatus())
                .skill(generation.getSkill().getName())
                .difficulty(mcq.getDifficulty())
                .title(mcq.getTitle())
                .description(mcq.getDescription())
                .marks(mcq.getMarks())
                .options(mcq.getOptions())
                .multipleChoice(mcq.isMultipleChoice())
                .explanation(mcq.getExplanation())
                .validationMessage(generation.getValidationMessage())
                .build();
    }

    private McqQuestionResponse toResponse(McqQuestion question) {
        McqQuestionResponse response = new McqQuestionResponse();
        response.setId(question.getId());
        response.setTitle(question.getTitle());
        response.setDescription(question.getDescription());
        response.setDifficulty(question.getDifficulty());
        response.setMarks(question.getMarks());
        response.setType(question.getType());
        response.setOptions(question.getOptions());
        response.setMultipleChoice(question.isMultipleChoice());
        response.setNegativeMarking(question.getNegativeMarking());
        response.setPartialMarking(question.isPartialMarking());
        response.setRandomizeOptions(question.isRandomizeOptions());
        response.setExplanation(question.getExplanation());
        return response;
    }
}
