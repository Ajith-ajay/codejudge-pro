package com.ajith.codejudge.problems.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ajith.codejudge.problems.dto.ProblemDto;
import com.ajith.codejudge.problems.entity.Problem;
import com.ajith.codejudge.problems.repository.ProblemRepository;
import com.ajith.codejudge.problems.service.interfaces.ProblemService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProblemServiceImpl implements ProblemService {

    private final ProblemRepository repo;

    @Autowired
    public ProblemServiceImpl(ProblemRepository repo) {
        this.repo = repo;
    }

    @Override
    public Page<ProblemDto> getProblems(String search, String difficulty, String tag, Pageable pageable) {
        Page<Problem> page = repo.findAllFiltered(
                (search == null || search.isBlank()) ? null : search,
                (difficulty == null || difficulty.isBlank()) ? null : difficulty,
                (tag == null || tag.isBlank()) ? null : tag,
                pageable);
        return page.map(ProblemDto::new);
    }
}
