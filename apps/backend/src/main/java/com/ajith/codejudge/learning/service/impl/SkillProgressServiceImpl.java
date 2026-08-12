package com.ajith.codejudge.learning.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ajith.codejudge.learning.dto.response.SkillProgressResponse;
import com.ajith.codejudge.learning.entity.UserSkillProgress;
import com.ajith.codejudge.learning.repository.UserSkillProgressRepository;
import com.ajith.codejudge.learning.service.SkillProgressService;
import com.ajith.codejudge.question.entity.Question;
import com.ajith.codejudge.question.entity.QuestionType;
import com.ajith.codejudge.skill.entity.Skill;
import com.ajith.codejudge.submission.entity.Submission;
import com.ajith.codejudge.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SkillProgressServiceImpl implements SkillProgressService {

    private final UserSkillProgressRepository progressRepository;

    @Override
    @Transactional
    public void updateFromSubmission(Submission submission) {
        if (submission != null && submission.getCandidate() != null) {
            return;
        }

        if (submission == null
                || submission.getUser() == null
                || submission.getQuestion() == null
                || submission.getQuestion().getSkills().isEmpty()) {
            return;
        }

        Question question = submission.getQuestion();
        double performance = question.getMarks() <= 0
                ? 0
                : Math.max(0, Math.min(100,
                        (submission.getScore() * 100.0) / question.getMarks()));

        boolean fullyCorrect
                = submission.getStatus() == com.ajith.codejudge.submission.entity.SubmissionStatus.ACCEPTED
                && performance >= 100.0;

        for (Skill skill : question.getSkills()) {
            UserSkillProgress progress = getOrCreate(submission.getUser(), skill);

            double previous = progress.getMasteryScore();

            // The first result establishes a baseline without allowing one
            // question to permanently define mastery.
            double learningRate = progress.getAttempts() == 0 ? 0.60 : 0.25;

            double updatedMastery
                    = previous + learningRate * (performance - previous);

            progress.setMasteryScore(
                    round(Math.max(0, Math.min(100, updatedMastery)))
            );

            double confidence = progress.getConfidenceScore();

            if (fullyCorrect) {
                confidence += 8;
            } else if (performance >= 50) {
                confidence += 3;
            } else {
                confidence -= 2;
            }

            progress.setConfidenceScore(
                    round(Math.max(0, Math.min(100, confidence)))
            );

            progress.setAttempts(progress.getAttempts() + 1);
            progress.setCorrectAttempts(
                    progress.getCorrectAttempts() + (fullyCorrect ? 1 : 0)
            );

            if (question.getType() == QuestionType.CODING) {
                progress.setCodingAttempts(progress.getCodingAttempts() + 1);
                progress.setCodingCorrect(progress.getCodingCorrect() + (fullyCorrect ? 1 : 0));
            } else {
                progress.setMcqAttempts(progress.getMcqAttempts() + 1);
                progress.setMcqCorrect(progress.getMcqCorrect() + (fullyCorrect ? 1 : 0));
            }

            progress.setLastAttemptedAt(LocalDateTime.now());
            progress.setUpdatedAt(LocalDateTime.now());
            progressRepository.save(progress);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillProgressResponse> getUserProgress(Long userId) {
        return progressRepository.findByUserIdOrderByMasteryScoreAsc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public UserSkillProgress getOrCreate(User user, Skill skill) {
        return progressRepository.findByUserIdAndSkillId(user.getId(), skill.getId())
                .orElseGet(() -> progressRepository.save(UserSkillProgress.builder()
                .user(user)
                .skill(skill)
                .build()));
    }

    private SkillProgressResponse toResponse(UserSkillProgress progress) {
        Skill skill = progress.getSkill();
        return SkillProgressResponse.builder()
                .skillId(skill.getId())
                .skillName(skill.getName())
                .category(skill.getCategory())
                .masteryScore(progress.getMasteryScore())
                .confidenceScore(progress.getConfidenceScore())
                .attempts(progress.getAttempts())
                .correctAttempts(progress.getCorrectAttempts())
                .codingAttempts(progress.getCodingAttempts())
                .codingCorrect(progress.getCodingCorrect())
                .mcqAttempts(progress.getMcqAttempts())
                .mcqCorrect(progress.getMcqCorrect())
                .build();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
