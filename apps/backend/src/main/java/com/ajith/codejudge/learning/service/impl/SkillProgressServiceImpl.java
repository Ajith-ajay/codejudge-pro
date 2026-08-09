package com.ajith.codejudge.learning.service.impl;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillProgressServiceImpl implements SkillProgressService {

    private final UserSkillProgressRepository progressRepository;

    @Override
    @Transactional
    public void updateFromSubmission(Submission submission) {
        if (submission.getCandidate() != null || submission.getQuestion().getSkills().isEmpty()) {
            return;
        }

        Question question = submission.getQuestion();
        double performance = question.getMarks() <= 0
                ? 0
                : Math.max(0, Math.min(100,
                (submission.getScore() * 100.0) / question.getMarks()));

        boolean correct = submission.getStatus().name().equals("ACCEPTED") && performance > 0;

        for (Skill skill : question.getSkills()) {
            UserSkillProgress progress = getOrCreate(submission.getUser(), skill);
            double previous = progress.getMasteryScore();
            double learningRate = progress.getAttempts() == 0 ? 1.0 : 0.30;
            double updatedMastery = previous + learningRate * (performance - previous);

            progress.setMasteryScore(round(Math.max(0, Math.min(100, updatedMastery))));
            progress.setConfidenceScore(Math.min(100, progress.getConfidenceScore() + 8));
            progress.setAttempts(progress.getAttempts() + 1);
            progress.setCorrectAttempts(progress.getCorrectAttempts() + (correct ? 1 : 0));

            if (question.getType() == QuestionType.CODING) {
                progress.setCodingAttempts(progress.getCodingAttempts() + 1);
                progress.setCodingCorrect(progress.getCodingCorrect() + (correct ? 1 : 0));
            } else {
                progress.setMcqAttempts(progress.getMcqAttempts() + 1);
                progress.setMcqCorrect(progress.getMcqCorrect() + (correct ? 1 : 0));
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
