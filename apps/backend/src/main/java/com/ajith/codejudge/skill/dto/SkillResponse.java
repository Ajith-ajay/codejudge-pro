package com.ajith.codejudge.skill.dto;

import com.ajith.codejudge.skill.entity.Skill;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SkillResponse {
    private Long id;
    private String name;
    private String category;

    public static SkillResponse from(Skill skill) {
        return SkillResponse.builder()
                .id(skill.getId())
                .name(skill.getName())
                .category(skill.getCategory())
                .build();
    }
}
