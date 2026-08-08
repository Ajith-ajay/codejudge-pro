package com.ajith.codejudge.question.service.interfaces;

import com.ajith.codejudge.common.pagination.PageRequestDto;
import com.ajith.codejudge.common.pagination.PageResponseDto;
import com.ajith.codejudge.question.dto.response.ProblemListResponse;

public interface ProblemService {

    PageResponseDto<ProblemListResponse> getProblems(
            PageRequestDto pageRequest,
            String difficulty,
            String status,
            String search
    );
}
