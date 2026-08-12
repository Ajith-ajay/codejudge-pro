package com.ajith.codejudge.problems.service.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ajith.codejudge.problems.dto.ProblemDto;

public interface ProblemService {

    Page<ProblemDto> getProblems(String search, String difficulty, String tag, Pageable pageable);
}
