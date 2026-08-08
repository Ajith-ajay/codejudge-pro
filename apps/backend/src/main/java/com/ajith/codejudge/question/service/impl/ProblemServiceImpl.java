package com.ajith.codejudge.question.service.impl;

import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ajith.codejudge.common.pagination.PageRequestDto;
import com.ajith.codejudge.common.pagination.PageResponseDto;
import com.ajith.codejudge.question.dto.response.ProblemListResponse;
import com.ajith.codejudge.question.dto.response.ProblemStatus;
import com.ajith.codejudge.question.entity.Difficulty;
import com.ajith.codejudge.question.repository.ProblemListProjection;
import com.ajith.codejudge.question.repository.ProblemRepository;
import com.ajith.codejudge.question.service.interfaces.ProblemService;
import com.ajith.codejudge.security.service.UserDetailsImpl;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProblemServiceImpl implements ProblemService {

    private final ProblemRepository problemRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<ProblemListResponse> getProblems(
            PageRequestDto pageRequest,
            String difficulty,
            String status,
            String search
    ) {

        Authentication authentication
                = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !(authentication.getPrincipal() instanceof UserDetailsImpl userDetails)) {

            throw new IllegalStateException(
                    "Authenticated user details not available"
            );
        }

        String normalizedDifficulty
                = normalizeDifficulty(difficulty);

        String normalizedStatus
                = normalizeStatus(status);

        String normalizedSearch
                = normalizeSearch(search);

        Page<ProblemListProjection> page
                = problemRepository.findProblemList(
                        userDetails.getId(),
                        normalizedDifficulty,
                        normalizedStatus,
                        normalizedSearch,
                        pageRequest.toPageable()
                );

        Page<ProblemListResponse> responsePage
                = page.map(this::toResponse);

        return PageResponseDto.fromPage(responsePage);
    }

    private ProblemListResponse toResponse(
            ProblemListProjection projection
    ) {

        return ProblemListResponse.builder()
                .id(projection.getId())
                .title(projection.getTitle())
                .difficulty(
                        Difficulty.valueOf(
                                projection.getDifficulty()
                        )
                )
                .status(
                        ProblemStatus.valueOf(
                                projection.getStatus()
                        )
                )
                .acceptanceRate(
                        projection.getAcceptanceRate() == null
                        ? 0.0
                        : projection.getAcceptanceRate()
                )
                .solvedUsers(
                        projection.getSolvedUsers() == null
                        ? 0L
                        : projection.getSolvedUsers()
                )
                .totalSubmissions(
                        projection.getTotalSubmissions() == null
                        ? 0L
                        : projection.getTotalSubmissions()
                )
                .build();
    }

    private String normalizeDifficulty(String difficulty) {

        if (difficulty == null || difficulty.isBlank()) {
            return null;
        }

        String value
                = difficulty.trim().toUpperCase(Locale.ROOT);

        try {
            return Difficulty.valueOf(value).name();
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid difficulty. Allowed values: EASY, MEDIUM, HARD"
            );
        }
    }

    private String normalizeStatus(String status) {

        if (status == null || status.isBlank()) {
            return null;
        }

        String value
                = status.trim().toUpperCase(Locale.ROOT);

        try {
            return ProblemStatus.valueOf(value).name();
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid status. Allowed values: "
                    + "SOLVED, ATTEMPTED, NOT_ATTEMPTED"
            );
        }
    }

    private String normalizeSearch(String search) {

        if (search == null || search.isBlank()) {
            return null;
        }

        return search.trim();
    }
}
