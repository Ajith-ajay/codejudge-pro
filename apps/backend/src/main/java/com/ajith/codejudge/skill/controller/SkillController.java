package com.ajith.codejudge.skill.controller;

import com.ajith.codejudge.common.response.ApiResponse;
import com.ajith.codejudge.skill.dto.SkillResponse;
import com.ajith.codejudge.skill.repository.SkillRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/skills")
@RequiredArgsConstructor
@Tag(name = "Skills", description = "Learning skill taxonomy")
public class SkillController {

    private final SkillRepository skillRepository;

    @GetMapping
    @Operation(summary = "List active learning skills")
    public ResponseEntity<ApiResponse<List<SkillResponse>>> getSkills() {
        List<SkillResponse> response = skillRepository.findAllByActiveTrueOrderByCategoryAscNameAsc()
                .stream()
                .map(SkillResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response, "Skills retrieved successfully"));
    }
}
