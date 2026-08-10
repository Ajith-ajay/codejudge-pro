package com.ajith.codejudge.learning.service.impl;

import com.ajith.codejudge.exception.BadRequestException;
import com.ajith.codejudge.exception.ResourceNotFoundException;
import com.ajith.codejudge.learning.dto.request.CreateAdaptiveAssessmentRequest;
import com.ajith.codejudge.learning.dto.response.AssessmentQuestionResponse;
import com.ajith.codejudge.learning.dto.response.LearningAssessmentResponse;
import com.ajith.codejudge.learning.entity.*;
import com.ajith.codejudge.learning.repository.LearningAssessmentRepository;
import com.ajith.codejudge.learning.repository.UserSkillProgressRepository;
import com.ajith.codejudge.learning.service.AdaptiveAssessmentService;
import com.ajith.codejudge.question.entity.Difficulty;
import com.ajith.codejudge.question.entity.Question;
import com.ajith.codejudge.question.entity.QuestionType;
import com.ajith.codejudge.question.repository.QuestionRepository;
import com.ajith.codejudge.skill.entity.Skill;
import com.ajith.codejudge.skill.repository.SkillRepository;
import com.ajith.codejudge.submission.entity.SubmissionStatus;
import com.ajith.codejudge.submission.repository.SubmissionRepository;
import com.ajith.codejudge.user.entity.User;
import com.ajith.codejudge.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdaptiveAssessmentServiceImpl implements AdaptiveAssessmentService {

    private final LearningAssessmentRepository assessmentRepository;
    private final UserSkillProgressRepository progressRepository;
    private final SkillRepository skillRepository;
    private final QuestionRepository questionRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public LearningAssessmentResponse create(Long userId, CreateAdaptiveAssessmentRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Skill skill = resolveSkill(userId, request.getSkillId());
        double mastery = progressRepository.findByUserIdAndSkillId(userId, skill.getId())
                .map(p -> p.getMasteryScore())
                .orElse(0.0);

        Difficulty targetDifficulty = chooseDifficulty(mastery);

        List<Question> selected = new ArrayList<>();
        selected.addAll(selectAdaptiveQuestions(
                userId,
                skill.getId(),
                QuestionType.MCQ,
                targetDifficulty,
                request.getMcqCount()
        ));
        selected.addAll(selectAdaptiveQuestions(
                userId,
                skill.getId(),
                QuestionType.CODING,
                targetDifficulty,
                request.getCodingCount()
        ));

        int expected = request.getMcqCount() + request.getCodingCount();
        if (selected.size() < expected) {
            throw new BadRequestException(
                    "Not enough unsolved questions for skill '" + skill.getName() + "' at the current difficulty. " +
                    "Available: " + selected.size() + ", required: " + expected);
        }

        LearningAssessment assessment = LearningAssessment.builder()
                .user(user)
                .skill(skill)
                .status(LearningAssessmentStatus.GENERATED)
                .totalQuestions(selected.size())
                .mcqCount(request.getMcqCount())
                .codingCount(request.getCodingCount())
                .targetDifficulty(targetDifficulty.name())
                .build();

        assessment = assessmentRepository.save(assessment);

        int order = 1;
        for (Question question : selected) {
            assessment.addQuestion(AssessmentQuestion.builder()
                    .assessmentId(assessment.getId())
                    .questionId(question.getId())
                    .assessment(assessment)
                    .question(question)
                    .orderIndex(order++)
                    .build());
        }

        assessmentRepository.save(assessment);
        return toResponse(assessment);
    }

    @Override
    @Transactional(readOnly = true)
    public LearningAssessmentResponse get(Long userId, Long assessmentId) {
        LearningAssessment assessment = getOwned(userId, assessmentId);
        return toResponse(assessment);
    }

    @Override
    @Transactional
    public LearningAssessmentResponse start(Long userId, Long assessmentId) {
        LearningAssessment assessment = getOwned(userId, assessmentId);

        if (assessment.getStatus() == LearningAssessmentStatus.COMPLETED) {
            throw new BadRequestException("Completed assessment cannot be started");
        }

        if (assessment.getQuestions().isEmpty()) {
            throw new BadRequestException("Assessment has no questions");
        }

        assessment.setStatus(LearningAssessmentStatus.IN_PROGRESS);
        return toResponse(assessmentRepository.save(assessment));
    }

    @Override
    @Transactional
    public LearningAssessmentResponse complete(Long userId, Long assessmentId) {
        LearningAssessment assessment = getOwned(userId, assessmentId);

        if (assessment.getStatus() == LearningAssessmentStatus.COMPLETED) {
            return toResponse(assessment);
        }

        List<Long> questionIds = assessment.getQuestions().stream()
                .map(AssessmentQuestion::getQuestionId)
                .toList();

        if (questionIds.isEmpty()) {
            throw new BadRequestException("Assessment has no questions");
        }

        List<com.ajith.codejudge.submission.entity.Submission> submissions =
                submissionRepository.findByAssessmentIdAndUserId(assessmentId, userId);

        Map<Long, Integer> bestScores = submissions.stream()
                .filter(s -> s.getCandidate() == null)
                .filter(s -> s.getStatus() != SubmissionStatus.PENDING
                        && s.getStatus() != SubmissionStatus.RUNNING)
                .collect(Collectors.toMap(
                        s -> s.getQuestion().getId(),
                        s -> s.getScore(),
                        Math::max));

        if (bestScores.size() < questionIds.size()) {
            throw new BadRequestException("Complete all assessment questions before finishing the assessment");
        }

        double totalMarks = assessment.getQuestions().stream()
                .mapToDouble(q -> q.getQuestion().getMarks())
                .sum();
        double earned = bestScores.values().stream().mapToDouble(Integer::doubleValue).sum();

        assessment.setScore(totalMarks <= 0 ? 0 : Math.round((earned / totalMarks) * 10000.0) / 100.0);
        assessment.setStatus(LearningAssessmentStatus.COMPLETED);
        assessment.setCompletedAt(LocalDateTime.now());
        return toResponse(assessmentRepository.save(assessment));
    }

    private Skill resolveSkill(Long userId, Long requestedSkillId) {
        if (requestedSkillId != null) {
            return skillRepository.findById(requestedSkillId)
                    .filter(Skill::isActive)
                    .orElseThrow(() -> new ResourceNotFoundException("Skill not found or inactive"));
        }

        return progressRepository.findByUserIdOrderByMasteryScoreAsc(userId).stream()
                .map(p -> p.getSkill())
                .filter(Skill::isActive)
                .findFirst()
                .orElseGet(() -> skillRepository.findAllByActiveTrueOrderByCategoryAscNameAsc()
                        .stream()
                        .findFirst()
                        .orElseThrow(() -> new BadRequestException("No active skills are configured")));
    }

    private Difficulty chooseDifficulty(double mastery) {
        if (mastery < 40) return Difficulty.EASY;
        if (mastery < 70) return Difficulty.MEDIUM;
        return Difficulty.HARD;
    }

    private List<Question> selectAdaptiveQuestions(
            Long userId,
            Long skillId,
            QuestionType type,
            Difficulty targetDifficulty,
            int count) {

        if (count <= 0) {
            return List.of();
        }

        List<Difficulty> difficultyOrder = difficultyOrder(targetDifficulty);
        List<Question> selected = new ArrayList<>();

        for (Difficulty difficulty : difficultyOrder) {
            if (selected.size() >= count) {
                break;
            }

            List<Question> candidates = questionRepository.findAdaptiveCandidates(
                    skillId,
                    type,
                    difficulty,
                    userId
            );

            Collections.shuffle(candidates);

            Set<Long> selectedIds = selected.stream()
                    .map(Question::getId)
                    .collect(Collectors.toSet());

            candidates.stream()
                    .filter(question -> !selectedIds.contains(question.getId()))
                    .limit(count - selected.size())
                    .forEach(selected::add);
        }

        return selected;
    }

    private List<Difficulty> difficultyOrder(Difficulty target) {
        return switch (target) {
            case EASY -> List.of(
                    Difficulty.EASY,
                    Difficulty.MEDIUM,
                    Difficulty.HARD
            );
            case MEDIUM -> List.of(
                    Difficulty.MEDIUM,
                    Difficulty.EASY,
                    Difficulty.HARD
            );
            case HARD -> List.of(
                    Difficulty.HARD,
                    Difficulty.MEDIUM,
                    Difficulty.EASY
            );
        };
    }

    private LearningAssessment getOwned(Long userId, Long assessmentId) {
        return assessmentRepository.findByIdAndUserId(assessmentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found"));
    }

    private LearningAssessmentResponse toResponse(LearningAssessment assessment) {
        return LearningAssessmentResponse.builder()
                .id(assessment.getId())
                .skillId(assessment.getSkill() == null ? null : assessment.getSkill().getId())
                .skillName(assessment.getSkill() == null ? null : assessment.getSkill().getName())
                .status(assessment.getStatus())
                .totalQuestions(assessment.getTotalQuestions())
                .mcqCount(assessment.getMcqCount())
                .codingCount(assessment.getCodingCount())
                .targetDifficulty(assessment.getTargetDifficulty())
                .score(assessment.getScore())
                .questions(assessment.getQuestions().stream()
                        .sorted(Comparator.comparingInt(AssessmentQuestion::getOrderIndex))
                        .map(q -> AssessmentQuestionResponse.builder()
                                .order(q.getOrderIndex())
                                .questionId(q.getQuestionId())
                                .title(q.getQuestion().getTitle())
                                .difficulty(q.getQuestion().getDifficulty())
                                .type(q.getQuestion().getType())
                                .build())
                        .toList())
                .build();
    }
}
