package com.ajith.codejudge.learning.session.service;

import com.ajith.codejudge.exception.BadRequestException;
import com.ajith.codejudge.exception.ResourceNotFoundException;
import com.ajith.codejudge.learning.roadmap.entity.*;
import com.ajith.codejudge.learning.roadmap.repository.LearningRoadmapRepository;
import com.ajith.codejudge.learning.repository.UserSkillProgressRepository;
import com.ajith.codejudge.learning.session.dto.response.*;
import com.ajith.codejudge.learning.session.entity.*;
import com.ajith.codejudge.learning.session.repository.*;
import com.ajith.codejudge.question.entity.*;
import com.ajith.codejudge.question.repository.QuestionRepository;
import com.ajith.codejudge.skill.entity.Skill;
import com.ajith.codejudge.skill.repository.SkillRepository;
import com.ajith.codejudge.submission.entity.*;
import com.ajith.codejudge.submission.repository.SubmissionRepository;
import com.ajith.codejudge.user.entity.User;
import com.ajith.codejudge.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DailyLearningSessionServiceImpl implements DailyLearningSessionService {

    private final LearningSessionRepository sessionRepository;
    private final LearningSessionActivityRepository activityRepository;
    private final LearningSessionActivityQuestionRepository activityQuestionRepository;
    private final LearningRoadmapRepository roadmapRepository;
    private final QuestionRepository questionRepository;
    private final UserSkillProgressRepository progressRepository;
    private final SkillRepository skillRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public LearningSessionResponse getOrCreateToday(Long userId) {
        LocalDate today = LocalDate.now();

        Optional<LearningSession> existing =
                sessionRepository.findByUserIdAndSessionDate(userId, today);
        if (existing.isPresent()) {
            return get(userId, existing.get().getId());
        }

        LearningRoadmap roadmap = roadmapRepository
                .findFirstByUserIdAndStatusOrderByCreatedAtDesc(userId, LearningRoadmapStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("No active learning roadmap found"));

        if (today.isBefore(roadmap.getStartDate())) {
            throw new BadRequestException("Your learning roadmap has not started yet");
        }

        long offset = ChronoUnit.DAYS.between(roadmap.getStartDate(), today);
        if (offset >= roadmap.getDurationDays()) {
            roadmap.setStatus(LearningRoadmapStatus.COMPLETED);
            roadmap.setUpdatedAt(LocalDateTime.now());
            roadmapRepository.save(roadmap);
            throw new BadRequestException("Your learning roadmap has been completed");
        }

        int dayNumber = (int) offset + 1;
        LearningRoadmapDay day = roadmap.getDays().stream()
                .filter(d -> d.getDayNumber() == dayNumber)
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Roadmap day " + dayNumber + " is missing"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        LearningSession session = LearningSession.builder()
                .user(user)
                .roadmap(roadmap)
                .roadmapDay(day)
                .sessionDate(today)
                .status(LearningSessionStatus.NOT_STARTED)
                .build();

        Set<Long> sessionQuestionIds = new HashSet<>();

        for (LearningRoadmapActivity roadmapActivity : day.getActivities()) {
            LearningSessionActivity activity = LearningSessionActivity.builder()
                    .roadmapActivity(roadmapActivity)
                    .sequenceNo(roadmapActivity.getSequenceNo())
                    .build();

            populateQuestions(userId, activity, roadmapActivity, sessionQuestionIds);
            session.addActivity(activity);
        }

        session = sessionRepository.save(session);
        return get(userId, session.getId());
    }

    @Override
    @Transactional
    public LearningSessionResponse getCurrent(Long userId) {
        return getOrCreateToday(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public LearningSessionResponse get(Long userId, Long sessionId) {
        LearningSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning session not found"));
        return toResponse(session);
    }

    @Override
    @Transactional
    public LearningSessionResponse start(Long userId, Long sessionId) {
        LearningSession session = getOwned(sessionId, userId);
        if (session.getStatus() == LearningSessionStatus.COMPLETED) {
            throw new BadRequestException("Completed learning session cannot be started");
        }
        if (session.getStatus() == LearningSessionStatus.NOT_STARTED) {
            session.setStatus(LearningSessionStatus.IN_PROGRESS);
            session.setStartedAt(LocalDateTime.now());
        }
        session.setUpdatedAt(LocalDateTime.now());
        sessionRepository.save(session);
        return get(userId, sessionId);
    }

    @Override
    @Transactional
    public LearningSessionResponse startActivity(Long userId, Long activityId) {
        LearningSessionActivity activity = getOwnedActivity(activityId, userId);
        LearningSession session = activity.getSession();

        if (session.getStatus() == LearningSessionStatus.COMPLETED) {
            throw new BadRequestException("Learning session is already completed");
        }
        if (activity.getStatus() == LearningSessionActivityStatus.COMPLETED) {
            return get(userId, session.getId());
        }

        LearningSessionActivity previous = session.getActivities().stream()
                .filter(a -> a.getSequenceNo() < activity.getSequenceNo())
                .max(Comparator.comparingInt(LearningSessionActivity::getSequenceNo))
                .orElse(null);

        if (previous != null && previous.getStatus() != LearningSessionActivityStatus.COMPLETED) {
            throw new BadRequestException("Complete the previous learning activity first");
        }

        if (session.getStatus() == LearningSessionStatus.NOT_STARTED) {
            session.setStatus(LearningSessionStatus.IN_PROGRESS);
            session.setStartedAt(LocalDateTime.now());
        }

        activity.setStatus(LearningSessionActivityStatus.IN_PROGRESS);
        if (activity.getStartedAt() == null) {
            activity.setStartedAt(LocalDateTime.now());
        }
        session.setUpdatedAt(LocalDateTime.now());
        activityRepository.save(activity);
        sessionRepository.save(session);
        return get(userId, session.getId());
    }

    @Override
    @Transactional
    public LearningSessionResponse completeActivity(Long userId, Long activityId) {
        LearningSessionActivity activity = getOwnedActivity(activityId, userId);
        LearningSession session = activity.getSession();

        if (session.getStatus() == LearningSessionStatus.COMPLETED) {
            throw new BadRequestException("Learning session is already completed");
        }
        if (activity.getStatus() == LearningSessionActivityStatus.COMPLETED) {
            return get(userId, session.getId());
        }
        if (activity.getStatus() != LearningSessionActivityStatus.IN_PROGRESS) {
            throw new BadRequestException("Start the learning activity before completing it");
        }

        RoadmapActivityType type = activity.getRoadmapActivity().getActivityType();
        double score = 100.0;

        if (requiresQuestions(type)) {
            List<Long> questionIds = activity.getQuestions().stream()
                    .map(q -> q.getQuestion().getId())
                    .toList();

            if (questionIds.isEmpty()) {
                throw new BadRequestException("This activity has no assigned questions");
            }

            List<Submission> submissions =
                    submissionRepository.findByLearningSessionActivityIdAndUserId(activityId, userId);

            Map<Long, Integer> bestScores = submissions.stream()
                    .filter(s -> s.getCandidate() == null)
                    .filter(s -> s.getStatus() != SubmissionStatus.PENDING
                            && s.getStatus() != SubmissionStatus.RUNNING)
                    .collect(Collectors.toMap(
                            s -> s.getQuestion().getId(),
                            Submission::getScore,
                            Math::max));

            if (bestScores.keySet().containsAll(questionIds) == false) {
                throw new BadRequestException("Complete all assigned questions before completing this activity");
            }

            double maxMarks = activity.getQuestions().stream()
                    .mapToDouble(q -> q.getQuestion().getMarks())
                    .sum();
            double earned = activity.getQuestions().stream()
                    .mapToDouble(q -> Math.max(0,
                            bestScores.getOrDefault(q.getQuestion().getId(), 0)))
                    .sum();

            score = maxMarks == 0 ? 0 :
                    Math.round((earned / maxMarks) * 10000.0) / 100.0;
        }

        activity.setScore(score);
        activity.setStatus(LearningSessionActivityStatus.COMPLETED);
        activity.setCompletedAt(LocalDateTime.now());
        activityRepository.save(activity);
        session.setUpdatedAt(LocalDateTime.now());
        sessionRepository.save(session);
        return get(userId, session.getId());
    }

    @Override
    @Transactional
    public LearningSessionResponse complete(Long userId, Long sessionId) {
        LearningSession session = getOwned(sessionId, userId);
        if (session.getStatus() == LearningSessionStatus.COMPLETED) {
            return get(userId, sessionId);
        }

        boolean complete = session.getActivities().stream()
                .allMatch(a -> a.getStatus() == LearningSessionActivityStatus.COMPLETED);
        if (!complete) {
            throw new BadRequestException("Complete all daily learning activities first");
        }

        session.setStatus(LearningSessionStatus.COMPLETED);
        session.setCompletedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        sessionRepository.save(session);
        return get(userId, sessionId);
    }

    @Override
    @Transactional
    public LearningDashboardResponse getDashboard(Long userId) {
        LearningSessionResponse session = getOrCreateToday(userId);
        Optional<LearningSessionActivityResponse> next = session.getActivities().stream()
                .filter(a -> a.getStatus() != LearningSessionActivityStatus.COMPLETED)
                .findFirst();

        long completedSessions = sessionRepository.countCompletedByUserId(userId);
        long streak = calculateCurrentStreak(userId);

        return LearningDashboardResponse.builder()
                .sessionId(session.getId())
                .sessionDate(session.getSessionDate())
                .sessionStatus(session.getStatus().name())
                .completedActivities(session.getCompletedActivities())
                .totalActivities(session.getTotalActivities())
                .progressPercentage(session.getProgressPercentage())
                .nextActivity(next.map(LearningSessionActivityResponse::getInstructions).orElse(null))
                .nextActivityId(next.map(LearningSessionActivityResponse::getId).orElse(null))
                .completedSessions(completedSessions)
                .currentStreak(streak)
                .build();
    }

    private void populateQuestions(Long userId, LearningSessionActivity activity,
                                    LearningRoadmapActivity roadmapActivity,
                                    Set<Long> sessionQuestionIds) {
        RoadmapActivityType type = roadmapActivity.getActivityType();
        if (!requiresQuestions(type)) return;

        int mcq = 0, coding = 0;
        switch (type) {
            case MCQ -> mcq = questionCount(roadmapActivity.getEstimatedMinutes(), 5, 20);
            case CODING -> coding = questionCount(roadmapActivity.getEstimatedMinutes(), 30, 5);
            case MOCK_TEST -> {
                int total = Math.max(2, questionCount(roadmapActivity.getEstimatedMinutes(), 8, 20));
                mcq = Math.max(1, (int) Math.round(total * 0.6));
                coding = Math.max(1, total - mcq);
            }
            default -> { return; }
        }

        Set<Long> blocked = new HashSet<>(
                activityQuestionRepository.findQuestionIdsAssignedToActiveSessions(userId));
        blocked.addAll(sessionQuestionIds);
        Set<Long> selectedIds = new HashSet<>();
        List<Question> selected = new ArrayList<>();

        List<Skill> skills = new ArrayList<>(roadmapActivity.getSkills());
        skills.sort(Comparator.comparingDouble(skill ->
                progressRepository.findByUserIdAndSkillId(userId, skill.getId())
                        .map(p -> p.getMasteryScore()).orElse(0.0)));

        for (Skill skill : skills) {
            double mastery = progressRepository.findByUserIdAndSkillId(userId, skill.getId())
                    .map(p -> p.getMasteryScore()).orElse(0.0);
            Difficulty difficulty = chooseDifficulty(mastery);

            addCandidates(selected, selectedIds, blocked,
                    collectCandidates(userId, skill.getId(), QuestionType.MCQ, difficulty,
                            mcq - countType(selected, QuestionType.MCQ)));
            addCandidates(selected, selectedIds, blocked,
                    collectCandidates(userId, skill.getId(), QuestionType.CODING, difficulty,
                            coding - countType(selected, QuestionType.CODING)));

            if (countType(selected, QuestionType.MCQ) >= mcq
                    && countType(selected, QuestionType.CODING) >= coding) break;
        }

        if (countType(selected, QuestionType.MCQ) < mcq
                || countType(selected, QuestionType.CODING) < coding) {
            throw new BadRequestException(
                    "Not enough available questions for activity: " + roadmapActivity.getInstructions());
        }

        Collections.shuffle(selected);
        int order = 1;
        for (Question question : selected) {
            activity.addQuestion(LearningSessionActivityQuestion.builder()
                    .question(question)
                    .orderIndex(order++)
                    .build());
            sessionQuestionIds.add(question.getId());
        }
    }

    private List<Question> collectCandidates(Long userId, Long skillId, QuestionType type,
                                              Difficulty difficulty, int required) {
        if (required <= 0) return List.of();

        List<Question> candidates = new ArrayList<>(
                questionRepository.findAdaptiveCandidates(skillId, type, difficulty, userId));
        Collections.shuffle(candidates);
        return candidates.stream().limit(required).toList();
    }

    private void addCandidates(List<Question> selected, Set<Long> selectedIds,
                               Set<Long> blocked, List<Question> candidates) {
        for (Question q : candidates) {
            if (!blocked.contains(q.getId()) && selectedIds.add(q.getId())) {
                selected.add(q);
            }
        }
    }

    private int countType(List<Question> questions, QuestionType type) {
        return (int) questions.stream().filter(q -> q.getType() == type).count();
    }

    private boolean requiresQuestions(RoadmapActivityType type) {
        return type == RoadmapActivityType.MCQ
                || type == RoadmapActivityType.CODING
                || type == RoadmapActivityType.MOCK_TEST;
    }

    private int questionCount(int minutes, int minutesPerQuestion, int max) {
        return Math.max(1, Math.min(max,
                (int) Math.ceil(minutes / (double) minutesPerQuestion)));
    }

    private Difficulty chooseDifficulty(double mastery) {
        if (mastery < 40) return Difficulty.EASY;
        if (mastery < 70) return Difficulty.MEDIUM;
        return Difficulty.HARD;
    }

    private LearningSession getOwned(Long id, Long userId) {
        return sessionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning session not found"));
    }

    private LearningSessionActivity getOwnedActivity(Long id, Long userId) {
        return activityRepository.findOwned(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning session activity not found"));
    }

    private LearningSessionResponse toResponse(LearningSession session) {
        List<LearningSessionActivityResponse> activities = session.getActivities().stream()
                .sorted(Comparator.comparingInt(LearningSessionActivity::getSequenceNo))
                .map(a -> LearningSessionActivityResponse.builder()
                        .id(a.getId())
                        .sequenceNo(a.getSequenceNo())
                        .type(a.getRoadmapActivity().getActivityType())
                        .instructions(a.getRoadmapActivity().getInstructions())
                        .estimatedMinutes(a.getRoadmapActivity().getEstimatedMinutes())
                        .status(a.getStatus())
                        .score(a.getScore())
                        .questions(a.getQuestions().stream()
                                .sorted(Comparator.comparingInt(LearningSessionActivityQuestion::getOrderIndex))
                                .map(q -> LearningSessionQuestionResponse.builder()
                                        .order(q.getOrderIndex())
                                        .questionId(q.getQuestion().getId())
                                        .title(q.getQuestion().getTitle())
                                        .difficulty(q.getQuestion().getDifficulty())
                                        .type(q.getQuestion().getType())
                                        .build())
                                .toList())
                        .build())
                .toList();

        int total = activities.size();
        int completed = (int) activities.stream()
                .filter(a -> a.getStatus() == LearningSessionActivityStatus.COMPLETED)
                .count();

        return LearningSessionResponse.builder()
                .id(session.getId())
                .roadmapId(session.getRoadmap().getId())
                .roadmapDayNumber(session.getRoadmapDay().getDayNumber())
                .roadmapDayTitle(session.getRoadmapDay().getTitle())
                .sessionDate(session.getSessionDate())
                .status(session.getStatus())
                .startedAt(session.getStartedAt())
                .completedAt(session.getCompletedAt())
                .totalActivities(total)
                .completedActivities(completed)
                .progressPercentage(total == 0 ? 100 :
                        (int) Math.round(completed * 100.0 / total))
                .activities(activities)
                .build();
    }

    private long calculateCurrentStreak(Long userId) {
        LocalDate last = sessionRepository.findLastCompletedDate(userId);
        if (last == null) return 0;
        LocalDate today = LocalDate.now();
        if (!last.equals(today) && !last.equals(today.minusDays(1))) return 0;

        long streak = 0;
        LocalDate cursor = last;
        while (sessionRepository.findByUserIdAndSessionDate(userId, cursor)
                .map(s -> s.getStatus() == LearningSessionStatus.COMPLETED)
                .orElse(false)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }
}
