package com.ajith.codejudge.submission.service.interfaces;

import com.ajith.codejudge.submission.dto.request.SubmissionRequest;
import com.ajith.codejudge.submission.dto.response.SubmissionResponse;

import java.util.List;

public interface SubmissionService {

    SubmissionResponse submitSolution(SubmissionRequest request, Long userId);

    SubmissionResponse getSubmissionById(Long id, Long userId);

    List<SubmissionResponse> getSubmissionsByCandidate(Long candidateId);

    List<SubmissionResponse> getSubmissionsByQuestion(Long questionId);
}
