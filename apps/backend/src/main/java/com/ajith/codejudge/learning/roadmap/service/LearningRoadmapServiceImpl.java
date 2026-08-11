package com.ajith.codejudge.learning.roadmap.service;

import com.ajith.codejudge.ai.client.LlmClient;
import com.ajith.codejudge.exception.BadRequestException;
import com.ajith.codejudge.exception.ConflictException;
import com.ajith.codejudge.exception.ResourceNotFoundException;
import com.ajith.codejudge.learning.roadmap.dto.request.CreateLearningRoadmapRequest;
import com.ajith.codejudge.learning.roadmap.dto.response.*;
import com.ajith.codejudge.learning.roadmap.entity.*;
import com.ajith.codejudge.learning.roadmap.repository.LearningRoadmapRepository;
import com.ajith.codejudge.learning.entity.UserSkillProgress;
import com.ajith.codejudge.learning.repository.UserSkillProgressRepository;
import com.ajith.codejudge.skill.entity.Skill;
import com.ajith.codejudge.skill.repository.SkillRepository;
import com.ajith.codejudge.user.entity.User;
import com.ajith.codejudge.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LearningRoadmapServiceImpl implements LearningRoadmapService {

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final UserSkillProgressRepository progressRepository;
    private final LearningRoadmapRepository roadmapRepository;
    private final LlmClient llmClient;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public LearningRoadmapResponse generate(Long userId, CreateLearningRoadmapRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Skill> skills = skillRepository.findAllByActiveTrueOrderByCategoryAscNameAsc();
        if (skills.isEmpty()) {
            throw new BadRequestException("No active learning skills are configured");
        }

        Map<Long, UserSkillProgress> progressBySkill = progressRepository.findByUserIdOrderByMasteryScoreAsc(userId)
                .stream().collect(Collectors.toMap(p -> p.getSkill().getId(), p -> p));

        String prompt = buildPrompt(request, skills, progressBySkill);
        String rawJson = llmClient.generateRoadmap(prompt);

        try {
            JsonNode root = objectMapper.readTree(rawJson);
            validateGeneratedRoadmap(root, request, skills);

            LearningRoadmap active = roadmapRepository
                    .findFirstByUserIdAndStatusOrderByCreatedAtDesc(userId, LearningRoadmapStatus.ACTIVE)
                    .orElse(null);
            int version = active == null ? 1 : active.getVersion() + 1;
            if (active != null) {
                active.setStatus(LearningRoadmapStatus.ARCHIVED);
                active.setUpdatedAt(LocalDateTime.now());
                roadmapRepository.save(active);
            }

            LearningRoadmap roadmap = LearningRoadmap.builder()
                    .user(user)
                    .title(requiredText(root, "title"))
                    .summary(requiredText(root, "summary"))
                    .goal(request.getGoal().trim())
                    .targetRole(request.getTargetRole().trim())
                    .preferredLanguage(request.getPreferredLanguage().trim())
                    .durationDays(request.getDurationDays())
                    .dailyMinutes(request.getDailyMinutes())
                    .status(LearningRoadmapStatus.ACTIVE)
                    .version(version)
                    .generatedPayload(objectMapper.writeValueAsString(root))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            persistPlan(roadmap, root, skills);
            LearningRoadmap saved = roadmapRepository.save(roadmap);
            return toResponse(saved);
        } catch (ConflictException | ResourceNotFoundException | BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to create learning roadmap for userId={}", userId, ex);
            throw new BadRequestException("Generated learning roadmap is invalid: " + ex.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public LearningRoadmapResponse getCurrent(Long userId) {
        LearningRoadmap roadmap = roadmapRepository
                .findFirstByUserIdAndStatusOrderByCreatedAtDesc(userId, LearningRoadmapStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("No active learning roadmap found"));
        LearningRoadmap loaded = roadmapRepository.findOwned(roadmap.getId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning roadmap not found"));
        return toResponse(loaded);
    }

    @Override
    @Transactional(readOnly = true)
    public LearningRoadmapResponse get(Long userId, Long roadmapId) {
        LearningRoadmap roadmap = roadmapRepository.findOwned(roadmapId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning roadmap not found"));
        return toResponse(roadmap);
    }

    private void persistPlan(LearningRoadmap roadmap, JsonNode root, List<Skill> skills) {
        Map<Long, Skill> skillMap = skills.stream().collect(Collectors.toMap(Skill::getId, s -> s));
        JsonNode weeks = root.path("weeks");
        for (JsonNode week : weeks) {
            int weekNumber = week.path("weekNumber").asInt();
            for (JsonNode day : week.path("days")) {
                LearningRoadmapDay roadmapDay = LearningRoadmapDay.builder()
                        .weekNumber(weekNumber)
                        .dayNumber(day.path("dayNumber").asInt())
                        .title(requiredText(day, "title"))
                        .focus(requiredText(day, "focus"))
                        .build();
                for (JsonNode activity : day.path("activities")) {
                    JsonNode skillIds = activity.path("skillIds");
                    LearningRoadmapActivity roadmapActivity = LearningRoadmapActivity.builder()
                            .sequenceNo(roadmapDay.getActivities().size() + 1)
                            .activityType(RoadmapActivityType.valueOf(activity.path("type").asText()))
                            .instructions(requiredText(activity, "instructions"))
                            .estimatedMinutes(activity.path("estimatedMinutes").asInt())
                            .skills(new HashSet<>())
                            .build();
                    for (JsonNode skillIdNode : skillIds) {
                        Long skillId = skillIdNode.asLong();
                        Skill skill = skillMap.get(skillId);
                        if (skill == null) {
                            throw new BadRequestException("Generated roadmap referenced unknown skill: " + skillId);
                        }
                        roadmapActivity.getSkills().add(skill);
                    }
                    roadmapDay.addActivity(roadmapActivity);
                }
                roadmap.addDay(roadmapDay);
            }
        }
    }

    private void validateGeneratedRoadmap(JsonNode root, CreateLearningRoadmapRequest request, List<Skill> skills) {
        if (!root.isObject()) throw new BadRequestException("LLM roadmap must be a JSON object");
        if (!root.hasNonNull("title") || !root.hasNonNull("summary")) {
            throw new BadRequestException("LLM roadmap title and summary are required");
        }
        JsonNode weeks = root.path("weeks");
        if (!weeks.isArray() || weeks.isEmpty()) {
            throw new BadRequestException("LLM roadmap must contain at least one week");
        }

        Set<Long> validSkillIds = skills.stream().map(Skill::getId).collect(Collectors.toSet());
        Set<Integer> dayNumbers = new HashSet<>();
        int totalDays = 0;
        for (JsonNode week : weeks) {
            int weekNumber = week.path("weekNumber").asInt(0);
            if (weekNumber <= 0) throw new BadRequestException("Invalid roadmap week number");
            JsonNode days = week.path("days");
            if (!days.isArray() || days.isEmpty()) throw new BadRequestException("Every roadmap week needs days");
            for (JsonNode day : days) {
                int dayNumber = day.path("dayNumber").asInt(0);
                if (dayNumber <= 0 || dayNumber > request.getDurationDays() || !dayNumbers.add(dayNumber)) {
                    throw new BadRequestException("Roadmap must contain unique day numbers from 1 to durationDays");
                }
                JsonNode activities = day.path("activities");
                if (!activities.isArray() || activities.isEmpty()) throw new BadRequestException("Every roadmap day needs activities");
                int minutes = 0;
                for (JsonNode activity : activities) {
                    int activityMinutes = activity.path("estimatedMinutes").asInt(0);
                    if (activityMinutes <= 0 || activityMinutes > request.getDailyMinutes()) {
                        throw new BadRequestException("Activity minutes exceed the daily study-time constraint");
                    }
                    minutes += activityMinutes;
                    JsonNode skillIds = activity.path("skillIds");
                    if (!skillIds.isArray() || skillIds.isEmpty()) throw new BadRequestException("Every activity needs at least one skill");
                    for (JsonNode skillId : skillIds) {
                        if (!validSkillIds.contains(skillId.asLong())) {
                            throw new BadRequestException("Unknown skill ID in generated roadmap: " + skillId.asLong());
                        }
                    }
                }
                if (minutes > request.getDailyMinutes()) {
                    throw new BadRequestException("Day " + dayNumber + " exceeds daily study time: " + minutes + " minutes");
                }
                totalDays++;
            }
        }
        if (totalDays != request.getDurationDays()) {
            throw new BadRequestException("Generated roadmap must contain exactly " + request.getDurationDays() + " days, but contains " + totalDays);
        }
    }

    private String buildPrompt(CreateLearningRoadmapRequest request, List<Skill> skills, Map<Long, UserSkillProgress> progress) {
        String skillCatalog = skills.stream().map(skill -> {
            UserSkillProgress p = progress.get(skill.getId());
            double mastery = p == null ? 0 : p.getMasteryScore();
            double confidence = p == null ? 0 : p.getConfidenceScore();
            int attempts = p == null ? 0 : p.getAttempts();
            return String.format(Locale.ROOT,
                    "ID=%d | %s | category=%s | mastery=%.2f | confidence=%.2f | attempts=%d",
                    skill.getId(), skill.getName(), skill.getCategory(), mastery, confidence, attempts);
        }).collect(Collectors.joining("\n"));

        return """
                You are the adaptive learning planner for CodeJudgePro, a placement-preparation platform.

                Create a practical day-by-day learning roadmap for ONE student.

                Student goal: %s
                Target role: %s
                Preferred coding language: %s
                Duration: %d days
                Maximum study time per day: %d minutes

                Skill catalog and measured performance:
                %s

                Planning rules:
                1. Use ONLY skill IDs from the supplied catalog. Never invent a skill ID.
                2. Prioritize weak/low-confidence skills, but include spaced review of stronger skills.
                3. For placement preparation, balance DSA coding, MCQs, CS fundamentals, and periodic mock tests.
                4. Do not schedule only coding or only MCQs.
                5. A LEARN activity teaches concepts; MCQ is conceptual practice; CODING is programming practice; REVIEW revisits mistakes; MOCK_TEST mixes skills.
                6. Each day must stay within the student's daily-minute limit.
                7. Create exactly %d numbered days, numbered 1 through %d.
                8. Keep activities actionable and concrete. Do not write generic motivational content.
                9. Increase difficulty gradually as mastery improves; do not assume mastery that the metrics do not support.
                10. Include regular review days and mixed mock tests near the end of the roadmap.
                11. Return ONLY valid JSON matching the required schema.
                12. Do not use markdown or code fences.
                """.formatted(
                request.getGoal().trim(),
                request.getTargetRole().trim(),
                request.getPreferredLanguage().trim(),
                request.getDurationDays(),
                request.getDailyMinutes(),
                skillCatalog,
                request.getDurationDays(),
                request.getDurationDays());
    }

    private String requiredText(JsonNode node, String field) {
        String value = node.path(field).asText(null);
        if (value == null || value.isBlank()) throw new BadRequestException("Missing required roadmap field: " + field);
        return value.trim();
    }

    private LearningRoadmapResponse toResponse(LearningRoadmap roadmap) {
        return LearningRoadmapResponse.builder()
                .id(roadmap.getId())
                .title(roadmap.getTitle())
                .summary(roadmap.getSummary())
                .goal(roadmap.getGoal())
                .targetRole(roadmap.getTargetRole())
                .preferredLanguage(roadmap.getPreferredLanguage())
                .durationDays(roadmap.getDurationDays())
                .dailyMinutes(roadmap.getDailyMinutes())
                .status(roadmap.getStatus())
                .version(roadmap.getVersion())
                .createdAt(roadmap.getCreatedAt())
                .days(roadmap.getDays().stream().map(day -> RoadmapDayResponse.builder()
                        .weekNumber(day.getWeekNumber())
                        .dayNumber(day.getDayNumber())
                        .title(day.getTitle())
                        .focus(day.getFocus())
                        .activities(day.getActivities().stream().map(activity -> RoadmapActivityResponse.builder()
                                .sequenceNo(activity.getSequenceNo())
                                .activityType(activity.getActivityType())
                                .instructions(activity.getInstructions())
                                .estimatedMinutes(activity.getEstimatedMinutes())
                                .skills(activity.getSkills().stream()
                                        .map(skill -> SkillReferenceResponse.builder()
                                                .id(skill.getId())
                                                .name(skill.getName())
                                                .build())
                                        .toList())
                                .build()).toList())
                        .build()).toList())
                .build();
    }
}
