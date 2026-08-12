package com.ajith.codejudge.learning.service;

import com.ajith.codejudge.learning.dto.response.SkillProgressResponse;
import com.ajith.codejudge.learning.entity.UserSkillProgress;
import com.ajith.codejudge.skill.entity.Skill;
import com.ajith.codejudge.submission.entity.Submission;
import com.ajith.codejudge.user.entity.User;

import java.util.List;

public interface SkillProgressService {
    void updateFromSubmission(Submission submission);
    List<SkillProgressResponse> getUserProgress(Long userId);
    UserSkillProgress getOrCreate(User user, Skill skill);
}
