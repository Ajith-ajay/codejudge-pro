package com.ajith.codejudge.exam.service.interfaces;

import com.ajith.codejudge.common.pagination.PageRequestDto;
import com.ajith.codejudge.common.pagination.PageResponseDto;
import com.ajith.codejudge.exam.dto.request.CandidateInviteRequest;
import com.ajith.codejudge.exam.dto.request.ExamRequest;
import com.ajith.codejudge.exam.dto.response.ExamCandidateResponse;
import com.ajith.codejudge.exam.dto.response.ExamResponse;

import java.util.List;

public interface ExamService {

    ExamResponse createExam(ExamRequest request);

    ExamResponse updateExam(Long id, ExamRequest request);

    ExamResponse getExamById(Long id);

    PageResponseDto<ExamResponse> getAllExams(PageRequestDto pageRequest);

    void deleteExam(Long id);

    ExamResponse publishExam(Long id);

    ExamResponse closeExam(Long id);

    List<ExamCandidateResponse> inviteCandidates(Long examId, CandidateInviteRequest request);

    List<ExamCandidateResponse> getExamCandidates(Long examId);

    List<ExamResponse> getActiveExamsForCandidate(Long userId);

    ExamCandidateResponse startExam(Long examId, Long userId);

    ExamCandidateResponse completeExam(Long examId, Long userId);
}
