package com.ajith.codejudge.problems.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ajith.codejudge.problems.dto.ProblemDto;
import com.ajith.codejudge.problems.service.interfaces.ProblemService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/problems")
@Tag(name = "Problems", description = "Endpoints for managing problems")
public class ProblemController {

    private final ProblemService service;

    @Autowired
    public ProblemController(ProblemService service) {
        this.service = service;
    }

    /**
     * GET /api/problems Query params: - page (0-based, default 0) - size
     * (default 20) - search (optional) - difficulty (Easy|Medium|Hard) - tag
     * (single tag filter)
     */
    @GetMapping
    public Page<ProblemDto> listProblems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String tag) {

        return service.getProblems(search, difficulty, tag, PageRequest.of(page, size));
    }
}
