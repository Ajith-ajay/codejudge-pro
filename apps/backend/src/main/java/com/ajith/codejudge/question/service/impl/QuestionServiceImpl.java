package com.ajith.codejudge.question.service.impl;

import com.ajith.codejudge.common.pagination.PageRequestDto;
import com.ajith.codejudge.common.pagination.PageResponseDto;
import com.ajith.codejudge.exception.ConflictException;
import com.ajith.codejudge.exception.ResourceNotFoundException;
import com.ajith.codejudge.question.dto.request.CodingQuestionRequest;
import com.ajith.codejudge.question.dto.request.LanguageRequest;
import com.ajith.codejudge.question.dto.request.McqQuestionRequest;
import com.ajith.codejudge.question.dto.response.CodingQuestionResponse;
import com.ajith.codejudge.question.dto.response.LanguageResponse;
import com.ajith.codejudge.question.dto.response.McqQuestionResponse;
import com.ajith.codejudge.question.dto.response.QuestionResponse;
import com.ajith.codejudge.question.entity.CodingQuestion;
import com.ajith.codejudge.question.entity.Language;
import com.ajith.codejudge.question.entity.McqQuestion;
import com.ajith.codejudge.question.entity.Question;
import com.ajith.codejudge.question.entity.TestCase;
import com.ajith.codejudge.question.mapper.LanguageMapper;
import com.ajith.codejudge.question.mapper.QuestionMapper;
import com.ajith.codejudge.question.mapper.TestCaseMapper;
import com.ajith.codejudge.question.repository.CodingQuestionRepository;
import com.ajith.codejudge.question.repository.LanguageRepository;
import com.ajith.codejudge.question.repository.McqQuestionRepository;
import com.ajith.codejudge.question.repository.QuestionRepository;
import com.ajith.codejudge.question.service.interfaces.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final McqQuestionRepository mcqQuestionRepository;
    private final CodingQuestionRepository codingQuestionRepository;
    private final LanguageRepository languageRepository;

    private final QuestionMapper questionMapper;
    private final LanguageMapper languageMapper;
    private final TestCaseMapper testCaseMapper;

    @Override
    @Transactional
    public LanguageResponse createLanguage(LanguageRequest request) {
        if (languageRepository.findByCode(request.getCode().toLowerCase()).isPresent()) {
            throw new ConflictException("Language code identifier already exists: " + request.getCode());
        }

        Language language = languageMapper.toEntity(request);
        language.setCode(request.getCode().toLowerCase());
        language = languageRepository.save(language);
        log.info("Created compiler language: {}", language.getName());
        return languageMapper.toResponse(language);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LanguageResponse> getAllLanguages() {
        return languageRepository.findAll().stream()
                .map(languageMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LanguageResponse getLanguageById(Long id) {
        return languageRepository.findById(id)
                .map(languageMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Language not found with id: " + id));
    }

    @Override
    @Transactional
    public McqQuestionResponse createMcqQuestion(McqQuestionRequest request) {
        McqQuestion mcqQuestion = questionMapper.toEntity(request);
        mcqQuestion = mcqQuestionRepository.save(mcqQuestion);
        log.info("Created MCQ Question: {}", mcqQuestion.getTitle());
        return questionMapper.toResponse(mcqQuestion);
    }

    @Override
    @Transactional
    public CodingQuestionResponse createCodingQuestion(CodingQuestionRequest request) {
        CodingQuestion codingQuestion = questionMapper.toEntity(request);

        // Map allowed languages
        Set<Language> allowedLanguages = new HashSet<>(languageRepository.findAllById(request.getAllowedLanguageIds()));
        if (allowedLanguages.size() != request.getAllowedLanguageIds().size()) {
            throw new ResourceNotFoundException("One or more specified Language IDs not found");
        }
        codingQuestion.setAllowedLanguages(allowedLanguages);

        // Map test cases
        if (request.getTestCases() != null) {
            for (var tcRequest : request.getTestCases()) {
                TestCase testCase = testCaseMapper.toEntity(tcRequest);
                codingQuestion.addTestCase(testCase);
            }
        }

        codingQuestion = codingQuestionRepository.save(codingQuestion);
        log.info("Created Coding Question: {}", codingQuestion.getTitle());
        return questionMapper.toResponse(codingQuestion);
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionResponse getQuestionById(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + id));
        return questionMapper.toResponse(question);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<QuestionResponse> getAllQuestions(PageRequestDto pageRequest) {
        Pageable pageable = pageRequest.toPageable();
        Page<Question> questions = questionRepository.findAll(pageable);
        
        Page<QuestionResponse> responsePage = questions.map(questionMapper::toResponse);
        return PageResponseDto.fromPage(responsePage);
    }

    @Override
    @Transactional
    public McqQuestionResponse updateMcqQuestion(Long id, McqQuestionRequest request) {
        McqQuestion mcqQuestion = mcqQuestionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MCQ Question not found with id: " + id));

        questionMapper.updateEntity(request, mcqQuestion);
        mcqQuestion = mcqQuestionRepository.save(mcqQuestion);
        log.info("Updated MCQ Question: {}", mcqQuestion.getTitle());
        return questionMapper.toResponse(mcqQuestion);
    }

    @Override
    @Transactional
    public CodingQuestionResponse updateCodingQuestion(Long id, CodingQuestionRequest request) {
        CodingQuestion codingQuestion = codingQuestionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coding Question not found with id: " + id));

        questionMapper.updateEntity(request, codingQuestion);

        // Update allowed languages
        Set<Language> allowedLanguages = new HashSet<>(languageRepository.findAllById(request.getAllowedLanguageIds()));
        if (allowedLanguages.size() != request.getAllowedLanguageIds().size()) {
            throw new ResourceNotFoundException("One or more specified Language IDs not found");
        }
        codingQuestion.setAllowedLanguages(allowedLanguages);

        // Update test cases (clear old and re-add new ones to ensure clean database replacement)
        codingQuestion.getTestCases().clear();
        if (request.getTestCases() != null) {
            for (var tcRequest : request.getTestCases()) {
                TestCase testCase = testCaseMapper.toEntity(tcRequest);
                codingQuestion.addTestCase(testCase);
            }
        }

        codingQuestion = codingQuestionRepository.save(codingQuestion);
        log.info("Updated Coding Question: {}", codingQuestion.getTitle());
        return questionMapper.toResponse(codingQuestion);
    }

    @Override
    @Transactional
    public void deleteQuestion(Long id) {
        if (!questionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Question not found with id: " + id);
        }
        questionRepository.deleteById(id);
        log.info("Deleted question with id: {}", id);
    }
}
