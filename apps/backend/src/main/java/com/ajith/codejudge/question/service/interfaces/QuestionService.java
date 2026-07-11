package com.ajith.codejudge.question.service.interfaces;

import com.ajith.codejudge.common.pagination.PageRequestDto;
import com.ajith.codejudge.common.pagination.PageResponseDto;
import com.ajith.codejudge.question.dto.request.CodingQuestionRequest;
import com.ajith.codejudge.question.dto.request.LanguageRequest;
import com.ajith.codejudge.question.dto.request.McqQuestionRequest;
import com.ajith.codejudge.question.dto.response.CodingQuestionResponse;
import com.ajith.codejudge.question.dto.response.LanguageResponse;
import com.ajith.codejudge.question.dto.response.McqQuestionResponse;
import com.ajith.codejudge.question.dto.response.QuestionResponse;

import java.util.List;

public interface QuestionService {

    LanguageResponse createLanguage(LanguageRequest request);

    List<LanguageResponse> getAllLanguages();

    LanguageResponse getLanguageById(Long id);

    McqQuestionResponse createMcqQuestion(McqQuestionRequest request);

    CodingQuestionResponse createCodingQuestion(CodingQuestionRequest request);

    QuestionResponse getQuestionById(Long id);

    PageResponseDto<QuestionResponse> getAllQuestions(PageRequestDto pageRequest);

    McqQuestionResponse updateMcqQuestion(Long id, McqQuestionRequest request);

    CodingQuestionResponse updateCodingQuestion(Long id, CodingQuestionRequest request);

    void deleteQuestion(Long id);
}
