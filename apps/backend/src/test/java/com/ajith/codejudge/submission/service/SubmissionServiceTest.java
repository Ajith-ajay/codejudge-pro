package com.ajith.codejudge.submission.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ajith.codejudge.compiler.sandbox.DockerSandboxExecutor;
import com.ajith.codejudge.compiler.sandbox.ExecutionResult;
import com.ajith.codejudge.question.entity.CodingQuestion;
import com.ajith.codejudge.question.entity.Difficulty;
import com.ajith.codejudge.question.entity.Language;
import com.ajith.codejudge.question.entity.McqOption;
import com.ajith.codejudge.question.entity.McqQuestion;
import com.ajith.codejudge.question.entity.QuestionType;
import com.ajith.codejudge.question.entity.TestCase;
import com.ajith.codejudge.question.repository.LanguageRepository;
import com.ajith.codejudge.question.repository.QuestionRepository;
import com.ajith.codejudge.question.repository.TestCaseRepository;
import com.ajith.codejudge.submission.dto.request.SubmissionRequest;
import com.ajith.codejudge.submission.entity.Submission;
import com.ajith.codejudge.submission.entity.SubmissionStatus;
import com.ajith.codejudge.submission.mapper.SubmissionMapper;
import com.ajith.codejudge.submission.repository.SubmissionRepository;
import com.ajith.codejudge.submission.repository.SubmissionTestCaseRepository;
import com.ajith.codejudge.submission.service.impl.SubmissionServiceImpl;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private SubmissionTestCaseRepository submissionTestCaseRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private LanguageRepository languageRepository;

    @Mock
    private TestCaseRepository testCaseRepository;

    @Mock
    private SubmissionMapper submissionMapper;

    @Mock
    private DockerSandboxExecutor dockerSandboxExecutor;

    @InjectMocks
    private SubmissionServiceImpl submissionService;

    private Language pythonLanguage;
    private McqQuestion mcqQuestion;
    private CodingQuestion codingQuestion;
    private TestCase testCase;

    @BeforeEach
    void setUp() {
        pythonLanguage = Language.builder()
                .id(1L)
                .name("Python")
                .code("python")
                .compilerVersion("3.11")
                .build();

        List<McqOption> options = new ArrayList<>();
        options.add(new McqOption("A", "Option A", true));
        options.add(new McqOption("B", "Option B", false));

        mcqQuestion = McqQuestion.builder()
                .id(10L)
                .title("MCQ Question")
                .description("Simple MCQ")
                .difficulty(Difficulty.EASY)
                .marks(10)
                .type(QuestionType.MCQ)
                .options(options)
                .isMultipleChoice(false)
                .negativeMarking(BigDecimal.ZERO)
                .partialMarking(false)
                .build();

        codingQuestion = CodingQuestion.builder()
                .id(20L)
                .title("Coding Question")
                .description("Simple Coding")
                .difficulty(Difficulty.MEDIUM)
                .marks(20)
                .type(QuestionType.CODING)
                .timeLimitMs(1000)
                .memoryLimitMb(256)
                .build();

        testCase = TestCase.builder()
                .id(30L)
                .codingQuestion(codingQuestion)
                .input("5")
                .expectedOutput("10")
                .hidden(false)
                .marks(10)
                .build();
    }

    @Test
    void submitSolution_Mcq_CorrectAnswer_Success() {
        // Arrange
        SubmissionRequest request = SubmissionRequest.builder()
                .questionId(10L)
                .languageId(1L)
                .sourceCode("A")
                .build();

        when(questionRepository.findById(10L)).thenReturn(Optional.of(mcqQuestion));
        when(languageRepository.findById(1L)).thenReturn(Optional.of(pythonLanguage));
        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        submissionService.submitSolution(request, 1L);

        // Assert
        verify(submissionRepository, atLeastOnce()).save(argThat(submission ->
                submission.getStatus() == SubmissionStatus.ACCEPTED && submission.getScore() == 10
        ));
    }

    @Test
    void submitSolution_Mcq_WrongAnswer() {
        // Arrange
        SubmissionRequest request = SubmissionRequest.builder()
                .questionId(10L)
                .languageId(1L)
                .sourceCode("B")
                .build();

        when(questionRepository.findById(10L)).thenReturn(Optional.of(mcqQuestion));
        when(languageRepository.findById(1L)).thenReturn(Optional.of(pythonLanguage));
        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        submissionService.submitSolution(request, 1L);

        // Assert
        verify(submissionRepository, atLeastOnce()).save(argThat(submission ->
                submission.getStatus() == SubmissionStatus.WRONG_ANSWER && submission.getScore() == 0
        ));
    }

    @Test
    void submitSolution_Coding_Accepted_Success() {
        // Arrange
        SubmissionRequest request = SubmissionRequest.builder()
                .questionId(20L)
                .languageId(1L)
                .sourceCode("print(int(input())*2)")
                .build();

        when(questionRepository.findById(20L)).thenReturn(Optional.of(codingQuestion));
        when(languageRepository.findById(1L)).thenReturn(Optional.of(pythonLanguage));
        when(testCaseRepository.findByCodingQuestionId(20L)).thenReturn(Collections.singletonList(testCase));
        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExecutionResult execResult = ExecutionResult.builder()
                .status(SubmissionStatus.ACCEPTED)
                .output("10")
                .timeMs(150)
                .memoryMb(12)
                .build();
        when(dockerSandboxExecutor.executeCode(anyString(), anyString(), anyString(), anyInt(), anyInt())).thenReturn(execResult);

        // Act
        submissionService.submitSolution(request, 1L);

        // Assert
        verify(submissionRepository, atLeastOnce()).save(argThat(submission ->
                submission.getStatus() == SubmissionStatus.ACCEPTED && submission.getScore() == 10
        ));
    }

    @Test
    void submitSolution_Coding_TimeLimitExceeded() {
        // Arrange
        SubmissionRequest request = SubmissionRequest.builder()
                .questionId(20L)
                .languageId(1L)
                .sourceCode("while True: pass")
                .build();

        when(questionRepository.findById(20L)).thenReturn(Optional.of(codingQuestion));
        when(languageRepository.findById(1L)).thenReturn(Optional.of(pythonLanguage));
        when(testCaseRepository.findByCodingQuestionId(20L)).thenReturn(Collections.singletonList(testCase));
        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExecutionResult execResult = ExecutionResult.builder()
                .status(SubmissionStatus.TIME_LIMIT_EXCEEDED)
                .errorMessage("Time Limit Exceeded")
                .timeMs(1000)
                .build();
        when(dockerSandboxExecutor.executeCode(anyString(), anyString(), anyString(), anyInt(), anyInt())).thenReturn(execResult);

        // Act
        submissionService.submitSolution(request, 1L);

        // Assert
        verify(submissionRepository, atLeastOnce()).save(argThat(submission ->
                submission.getStatus() == SubmissionStatus.TIME_LIMIT_EXCEEDED && submission.getScore() == 0
        ));
    }
}
