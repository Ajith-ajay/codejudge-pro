package com.ajith.codejudge.ai.tutor.service;

import com.ajith.codejudge.ai.client.LlmClient;
import com.ajith.codejudge.ai.tutor.dto.request.TutorChatRequest;
import com.ajith.codejudge.ai.tutor.dto.response.TutorChatResponse;
import com.ajith.codejudge.ai.tutor.dto.response.TutorConversationResponse;
import com.ajith.codejudge.ai.tutor.entity.AiTutorConversation;
import com.ajith.codejudge.ai.tutor.entity.AiTutorMessage;
import com.ajith.codejudge.ai.tutor.entity.AiTutorMessageRole;
import com.ajith.codejudge.ai.tutor.repository.AiTutorConversationRepository;
import com.ajith.codejudge.ai.tutor.repository.AiTutorMessageRepository;
import com.ajith.codejudge.exception.BadRequestException;
import com.ajith.codejudge.exception.ResourceNotFoundException;
import com.ajith.codejudge.learning.entity.UserSkillProgress;
import com.ajith.codejudge.learning.roadmap.entity.LearningRoadmap;
import com.ajith.codejudge.learning.roadmap.repository.LearningRoadmapRepository;
import com.ajith.codejudge.learning.repository.UserSkillProgressRepository;
import com.ajith.codejudge.question.entity.Question;
import com.ajith.codejudge.question.repository.QuestionRepository;
import com.ajith.codejudge.skill.entity.Skill;
import com.ajith.codejudge.skill.repository.SkillRepository;
import com.ajith.codejudge.submission.entity.Submission;
import com.ajith.codejudge.submission.repository.SubmissionRepository;
import com.ajith.codejudge.user.entity.User;
import com.ajith.codejudge.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiTutorServiceImpl implements AiTutorService {

    private static final int MAX_HISTORY_MESSAGES = 10;
    private static final int MAX_CONTEXT_PROGRESS = 15;
    private static final int MAX_CONTEXT_SUBMISSIONS = 8;
    private static final int MAX_TEXT = 10000;

    private final LlmClient llmClient;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final SubmissionRepository submissionRepository;
    private final LearningRoadmapRepository roadmapRepository;
    private final SkillRepository skillRepository;
    private final UserSkillProgressRepository progressRepository;
    private final AiTutorConversationRepository conversationRepository;
    private final AiTutorMessageRepository messageRepository;

    @Override
    @Transactional
    public TutorChatResponse chat(Long userId, TutorChatRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Question question = resolveQuestion(userId, request.getQuestionId(), request.getSubmissionId());
        Submission submission = resolveSubmission(userId, request.getSubmissionId(), question);
        LearningRoadmap roadmap = resolveRoadmap(userId, request.getRoadmapId());
        Skill skill = resolveSkill(request.getSkillId());

        AiTutorConversation conversation = resolveConversation(user, request, question, submission, roadmap, skill);

        String history = buildHistory(conversation.getId());
        String context = buildContext(user, question, submission, roadmap, skill);

        String prompt = buildPrompt(request.getMessage().trim(), history, context);
        String rawResponse = llmClient.generateTutor(prompt);

        JsonNode response = parseResponse(rawResponse);

        String reply = requiredText(response, "reply");
        String diagnosis = response.path("diagnosis").asText("");
        String practiceSuggestion = response.path("practiceSuggestion").asText("");

        List<String> nextSteps = response.path("nextSteps").isArray()
                ? objectMapper.convertValue(response.path("nextSteps"),
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class))
                : List.of();

        AiTutorMessage userMessage = AiTutorMessage.builder()
                .conversation(conversation)
                .role(AiTutorMessageRole.USER)
                .content(request.getMessage().trim())
                .build();

        AiTutorMessage assistantMessage = AiTutorMessage.builder()
                .conversation(conversation)
                .role(AiTutorMessageRole.ASSISTANT)
                .content(reply)
                .build();

        messageRepository.save(userMessage);
        messageRepository.save(assistantMessage);

        conversation.setUpdatedAt(java.time.LocalDateTime.now());
        conversationRepository.save(conversation);

        return TutorChatResponse.builder()
                .conversationId(conversation.getId())
                .reply(reply)
                .diagnosis(diagnosis)
                .nextSteps(nextSteps)
                .practiceSuggestion(practiceSuggestion)
                .createdAt(assistantMessage.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TutorConversationResponse getConversation(Long userId, Long conversationId) {
        AiTutorConversation conversation = conversationRepository.findOwned(conversationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor conversation not found"));

        List<AiTutorMessage> messages = conversation.getMessages();

        return TutorConversationResponse.builder()
                .id(conversation.getId())
                .title(conversation.getTitle())
                .questionId(conversation.getQuestionId())
                .submissionId(conversation.getSubmissionId())
                .roadmapId(conversation.getRoadmapId())
                .skillId(conversation.getSkillId())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .messages(messages.stream()
                        .map(message -> TutorConversationResponse.MessageResponse.builder()
                                .id(message.getId())
                                .role(message.getRole())
                                .content(message.getContent())
                                .createdAt(message.getCreatedAt())
                                .build())
                        .toList())
                .build();
    }

    private AiTutorConversation resolveConversation(
            User user,
            TutorChatRequest request,
            Question question,
            Submission submission,
            LearningRoadmap roadmap,
            Skill skill) {

        if (request.getConversationId() != null) {
            return conversationRepository.findOwned(request.getConversationId(), user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tutor conversation not found"));
        }

        String title = request.getMessage().trim();
        if (title.length() > 255) {
            title = title.substring(0, 252) + "...";
        }

        return conversationRepository.save(
                AiTutorConversation.builder()
                        .user(user)
                        .title(title)
                        .questionId(question == null ? null : question.getId())
                        .submissionId(submission == null ? null : submission.getId())
                        .roadmapId(roadmap == null ? null : roadmap.getId())
                        .skillId(skill == null ? null : skill.getId())
                        .build()
        );
    }

    private Question resolveQuestion(Long userId, Long questionId, Long submissionId) {
        if (questionId != null) {
            return questionRepository.findById(questionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
        }

        if (submissionId != null) {
            Submission submission = submissionRepository.findById(submissionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

            if (!submission.getUser().getId().equals(userId)) {
                throw new BadRequestException("Submission does not belong to the current user");
            }
            return submission.getQuestion();
        }

        return null;
    }

    private Submission resolveSubmission(Long userId, Long submissionId, Question question) {
        if (submissionId == null) {
            return null;
        }

        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        if (!submission.getUser().getId().equals(userId)) {
            throw new BadRequestException("Submission does not belong to the current user");
        }

        if (question != null && !question.getId().equals(submission.getQuestion().getId())) {
            throw new BadRequestException("Question and submission do not belong together");
        }

        return submission;
    }

    private LearningRoadmap resolveRoadmap(Long userId, Long roadmapId) {
        if (roadmapId == null) {
            return null;
        }

        return roadmapRepository.findOwned(roadmapId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning roadmap not found"));
    }

    private Skill resolveSkill(Long skillId) {
        if (skillId == null) {
            return null;
        }

        return skillRepository.findById(skillId)
                .filter(Skill::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found or inactive"));
    }

    private String buildHistory(Long conversationId) {
        List<AiTutorMessage> messages =
                messageRepository.findTop10ByConversationIdOrderByCreatedAtDesc(conversationId);

        return messages.stream()
                .sorted(Comparator.comparing(AiTutorMessage::getCreatedAt))
                .map(m -> m.getRole().name() + ": " + truncate(m.getContent(), 4000))
                .collect(Collectors.joining("\n"));
    }

    private String buildContext(
            User user,
            Question question,
            Submission submission,
            LearningRoadmap roadmap,
            Skill selectedSkill) {

        StringBuilder context = new StringBuilder();

        context.append("STUDENT\n");
        context.append("id=").append(user.getId()).append('\n');
        context.append("name=")
                .append(user.getFirstName() == null ? "Student" : user.getFirstName())
                .append('\n');

        context.append("\nSKILL PROGRESS\n");
        List<UserSkillProgress> progress = progressRepository
                .findByUserIdOrderByMasteryScoreAsc(user.getId());

        progress.stream()
                .limit(MAX_CONTEXT_PROGRESS)
                .forEach(p -> context.append(String.format(
                        Locale.ROOT,
                        "- %s | mastery=%.1f | confidence=%.1f | attempts=%d | correct=%d | coding=%d/%d | mcq=%d/%d%n",
                        p.getSkill().getName(),
                        p.getMasteryScore(),
                        p.getConfidenceScore(),
                        p.getAttempts(),
                        p.getCorrectAttempts(),
                        p.getCodingCorrect(),
                        p.getCodingAttempts(),
                        p.getMcqCorrect(),
                        p.getMcqAttempts()
                )));

        if (selectedSkill != null) {
            context.append("\nSELECTED SKILL\n");
            context.append(selectedSkill.getId())
                    .append(" | ")
                    .append(selectedSkill.getName())
                    .append(" | category=")
                    .append(selectedSkill.getCategory())
                    .append('\n');
        }

        if (question != null) {
            context.append("\nCURRENT QUESTION\n");
            context.append("id=").append(question.getId()).append('\n');
            context.append("title=").append(question.getTitle()).append('\n');
            context.append("type=").append(question.getType()).append('\n');
            context.append("difficulty=").append(question.getDifficulty()).append('\n');
            context.append("skills=")
                    .append(question.getSkills().stream()
                            .map(Skill::getName)
                            .sorted()
                            .collect(Collectors.joining(", ")))
                    .append('\n');
            context.append("description=")
                    .append(truncate(question.getDescription(), MAX_TEXT))
                    .append('\n');
        }

        if (submission != null) {
            context.append("\nCURRENT SUBMISSION\n");
            context.append("id=").append(submission.getId()).append('\n');
            context.append("status=").append(submission.getStatus()).append('\n');
            context.append("score=").append(submission.getScore()).append('\n');
            context.append("executionTimeMs=").append(submission.getExecutionTimeMs()).append('\n');
            context.append("language=").append(submission.getLanguage().getCode()).append('\n');
            context.append("sourceCode=\n")
                    .append(truncate(submission.getSourceCode(), MAX_TEXT))
                    .append('\n');
        }

        if (roadmap != null) {
            context.append("\nLEARNING ROADMAP\n");
            context.append("title=").append(roadmap.getTitle()).append('\n');
            context.append("goal=").append(roadmap.getGoal()).append('\n');
            context.append("targetRole=").append(roadmap.getTargetRole()).append('\n');

            roadmap.getDays().stream()
                    .limit(3)
                    .forEach(day -> context.append(String.format(
                            "- Day %d: %s | focus=%s%n",
                            day.getDayNumber(),
                            day.getTitle(),
                            day.getFocus()
                    )));
        }

        context.append("\nRECENT PRACTICE SUBMISSIONS\n");
        submissionRepository.findByUserId(user.getId()).stream()
                .filter(s -> s.getCandidate() == null)
                .sorted(Comparator.comparing(
                        Submission::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(MAX_CONTEXT_SUBMISSIONS)
                .forEach(s -> context.append(String.format(
                        "- question=%d | status=%s | score=%d | time=%dms%n",
                        s.getQuestion().getId(),
                        s.getStatus(),
                        s.getScore(),
                        s.getExecutionTimeMs()
                )));

        return context.toString();
    }

    private String buildPrompt(String userMessage, String history, String context) {
        return """
                You are CodeJudgePro's personal placement-preparation teacher.

                Your job is to teach, not merely answer.
                Use the student's measured performance and supplied context.
                Never invent a submission result, skill score, question detail, roadmap item, or fact that is not present in the context.

                Teaching rules:
                1. Explain concepts from first principles when the student is confused.
                2. If the student shares a coding mistake, identify the likely cause and explain the reasoning.
                3. Do not immediately reveal a complete solution when a hint or guided approach is more educational, unless the student explicitly asks for the full solution.
                4. Prefer small examples and progressively harder steps.
                5. Connect advice to the student's weak skills when evidence supports it.
                6. For placement preparation, keep advice practical and interview-oriented.
                7. If the student is wrong, correct them respectfully and explicitly.
                8. Never claim to have executed code or verified output; execution results must come from the supplied submission context.
                9. Keep the response concise but useful.
                10. Return ONLY JSON matching the required tutor schema.

                STUDENT CONTEXT:
                %s

                CONVERSATION HISTORY:
                %s

                STUDENT MESSAGE:
                %s
                """.formatted(context, history.isBlank() ? "(new conversation)" : history, userMessage);
    }

    private JsonNode parseResponse(String raw) {
        try {
            JsonNode node = objectMapper.readTree(raw);
            if (!node.isObject()) {
                throw new BadRequestException("LLM tutor response must be a JSON object");
            }
            return node;
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BadRequestException("LLM tutor returned invalid JSON");
        }
    }

    private String requiredText(JsonNode node, String field) {
        String value = node.path(field).asText(null);
        if (value == null || value.isBlank()) {
            throw new BadRequestException("LLM tutor response is missing: " + field);
        }
        return value.trim();
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        return value.length() <= maxLength
                ? value
                : value.substring(0, maxLength) + "\n[truncated]";
    }
}
