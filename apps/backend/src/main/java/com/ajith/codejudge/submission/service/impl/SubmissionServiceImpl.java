package com.ajith.codejudge.submission.service.impl;

import com.ajith.codejudge.compiler.sandbox.DockerSandboxExecutor;
import com.ajith.codejudge.compiler.sandbox.ExecutionResult;
import com.ajith.codejudge.exam.entity.CandidateStatus;
import com.ajith.codejudge.exam.entity.ExamCandidate;
import com.ajith.codejudge.exam.repository.ExamCandidateRepository;
import com.ajith.codejudge.exception.BadRequestException;
import com.ajith.codejudge.exception.ForbiddenException;
import com.ajith.codejudge.exception.ResourceNotFoundException;
import com.ajith.codejudge.question.entity.CodingQuestion;
import com.ajith.codejudge.question.entity.Language;
import com.ajith.codejudge.question.entity.McqOption;
import com.ajith.codejudge.question.entity.McqQuestion;
import com.ajith.codejudge.question.entity.Question;
import com.ajith.codejudge.question.entity.QuestionType;
import com.ajith.codejudge.question.entity.TestCase;
import com.ajith.codejudge.question.repository.LanguageRepository;
import com.ajith.codejudge.question.repository.QuestionRepository;
import com.ajith.codejudge.question.repository.TestCaseRepository;
import com.ajith.codejudge.submission.dto.request.SubmissionRequest;
import com.ajith.codejudge.submission.dto.response.SubmissionResponse;
import com.ajith.codejudge.submission.dto.response.SubmissionTestCaseResponse;
import com.ajith.codejudge.submission.entity.Submission;
import com.ajith.codejudge.submission.entity.SubmissionStatus;
import com.ajith.codejudge.submission.entity.SubmissionTestCase;
import com.ajith.codejudge.submission.mapper.SubmissionMapper;
import com.ajith.codejudge.submission.repository.SubmissionRepository;
import com.ajith.codejudge.submission.repository.SubmissionTestCaseRepository;
import com.ajith.codejudge.submission.service.interfaces.SubmissionService;
import com.ajith.codejudge.exam.service.interfaces.LeaderboardService;
import com.ajith.codejudge.learning.service.SkillProgressService;
import com.ajith.codejudge.user.entity.User;
import com.ajith.codejudge.user.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final QuestionRepository questionRepository;
    private final LanguageRepository languageRepository;
    private final ExamCandidateRepository examCandidateRepository;
    private final TestCaseRepository testCaseRepository;

    private final SubmissionMapper submissionMapper;
    private final DockerSandboxExecutor dockerSandboxExecutor;
    private final LeaderboardService leaderboardService;
    private final UserRepository userRepository;
    private final SkillProgressService skillProgressService;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    @Transactional
    public SubmissionResponse submitSolution(SubmissionRequest request, Long userId) {
        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + request.getQuestionId()));

        Language language = languageRepository.findById(request.getLanguageId())
                .orElseThrow(() -> new ResourceNotFoundException("Language not found with id: " + request.getLanguageId()));

        ExamCandidate candidate = null;
        if (request.getCandidateId() != null) {
            candidate = examCandidateRepository.findById(request.getCandidateId())
                    .orElseThrow(() -> new ResourceNotFoundException("Candidate enrollment not found"));
            if (candidate.getStatus() != CandidateStatus.STARTED) {
                throw new ForbiddenException("You cannot submit solutions unless your exam session is started");
            }
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Initialize submission record as RUNNING
        Submission submission = Submission.builder()
                .user(user)
                .candidate(candidate)
                .question(question)
                .language(language)
                .sourceCode(request.getSourceCode())
                .status(SubmissionStatus.RUNNING)
                .build();
        submission = submissionRepository.save(submission);

        if (question.getType() == QuestionType.MCQ) {
            gradeMcqSubmission(submission, (McqQuestion) question);
        } else {
            gradeCodingSubmission(submission, (CodingQuestion) question, language);
        }

        submission = submissionRepository.save(submission);
        skillProgressService.updateFromSubmission(submission);
        log.info("Submission {} graded with final status: {} and score: {}", submission.getId(), submission.getStatus(), submission.getScore());

        if (candidate != null) {
            recalculateCandidateScore(candidate, submission.getCreatedAt() != null ? submission.getCreatedAt() : java.time.LocalDateTime.now());
        }

        return toSubmissionResponseSecure(submission);
    }

    private void recalculateCandidateScore(ExamCandidate candidate, java.time.LocalDateTime submissionTime) {
        try {
            List<Submission> submissions = submissionRepository.findByCandidateId(candidate.getId());
            
            // Group by question id and find the maximum score for each question
            java.util.Map<Long, Integer> maxScores = submissions.stream()
                    .collect(Collectors.toMap(
                            sub -> sub.getQuestion().getId(),
                            Submission::getScore,
                            Integer::max
                    ));

            int totalScore = maxScores.values().stream().mapToInt(Integer::intValue).sum();
            candidate.setScore(BigDecimal.valueOf(totalScore));
            examCandidateRepository.save(candidate);

            // Update the leaderboard in Redis
            leaderboardService.updateScore(candidate.getExam().getId(), candidate.getId(), totalScore, submissionTime);
        } catch (Exception e) {
            log.error("Failed to recalculate score or update leaderboard for candidate {}", candidate.getId(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SubmissionResponse getSubmissionById(Long id, Long userId) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + id));

        // Enforce candidate access restrictions
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isCandidate = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CANDIDATE"));
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") ||
                a.getAuthority().equals("ROLE_SUPER_ADMIN") ||
                a.getAuthority().equals("ROLE_EXAM_SETTER")
        );

        if (isCandidate && !isAdmin) {
            if (submission.getCandidate() == null || !submission.getCandidate().getUser().getId().equals(userId)) {
                throw new ForbiddenException("You are not authorized to view this submission");
            }
        }

        return toSubmissionResponseSecure(submission);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionResponse> getSubmissionsByCandidate(Long candidateId) {
        return submissionRepository.findByCandidateId(candidateId).stream()
                .map(this::toSubmissionResponseSecure)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionResponse> getSubmissionsByQuestion(Long questionId) {
        return submissionRepository.findByQuestionId(questionId).stream()
                .map(this::toSubmissionResponseSecure)
                .collect(Collectors.toList());
    }

    private void gradeMcqSubmission(Submission submission, McqQuestion mcq) {
        List<McqOption> options = mcq.getOptions();
        Set<String> correctOptionIds = options.stream()
                .filter(McqOption::isCorrect)
                .map(McqOption::getId)
                .collect(Collectors.toSet());

        Set<String> submittedOptionIds = parseSubmittedOptions(submission.getSourceCode());

        if (correctOptionIds.equals(submittedOptionIds)) {
            submission.setStatus(SubmissionStatus.ACCEPTED);
            submission.setScore(mcq.getMarks());
        } else {
            // Apply partial marking if allowed and no wrong options selected
            boolean hasWrongOption = submittedOptionIds.stream().anyMatch(id -> !correctOptionIds.contains(id));
            if (mcq.isPartialMarking() && !hasWrongOption && !submittedOptionIds.isEmpty()) {
                double fraction = (double) submittedOptionIds.size() / correctOptionIds.size();
                BigDecimal partialScore = BigDecimal.valueOf(mcq.getMarks())
                        .multiply(BigDecimal.valueOf(fraction))
                        .setScale(0, RoundingMode.HALF_UP);
                submission.setStatus(SubmissionStatus.ACCEPTED);
                submission.setScore(partialScore.intValue());
            } else {
                // Apply negative marking
                if (mcq.getNegativeMarking() != null && mcq.getNegativeMarking().compareTo(BigDecimal.ZERO) > 0) {
                    int penalty = mcq.getNegativeMarking().intValue();
                    submission.setScore(-penalty);
                } else {
                    submission.setScore(0);
                }
                submission.setStatus(SubmissionStatus.WRONG_ANSWER);
            }
        }
    }

    private void gradeCodingSubmission(Submission submission, CodingQuestion codingQuestion, Language language) {
        List<TestCase> testCases = testCaseRepository.findByCodingQuestionId(codingQuestion.getId());
        if (testCases.isEmpty()) {
            throw new BadRequestException("Coding Question has no test cases configured");
        }

        // Concurrent execution of test cases using Docker sandbox
        List<CompletableFuture<SubmissionTestCase>> futures = testCases.stream()
                .map(testCase -> CompletableFuture.supplyAsync(() -> {
                    ExecutionResult result = dockerSandboxExecutor.executeCode(
                            language.getCode(),
                            submission.getSourceCode(),
                            testCase.getInput(),
                            codingQuestion.getTimeLimitMs(),
                            codingQuestion.getMemoryLimitMb()
                    );

                    String status = "FAILED";
                    if (result.getStatus() == SubmissionStatus.ACCEPTED) {
                        boolean match = compareOutputs(result.getOutput(), testCase.getExpectedOutput());
                        status = match ? "PASSED" : "FAILED";
                    } else if (result.getStatus() == SubmissionStatus.TIME_LIMIT_EXCEEDED) {
                        status = "TLE";
                    } else if (result.getStatus() == SubmissionStatus.MEMORY_LIMIT_EXCEEDED) {
                        status = "MLE";
                    } else if (result.getStatus() == SubmissionStatus.COMPILATION_ERROR) {
                        status = "COMPILATION_ERROR";
                    } else if (result.getStatus() == SubmissionStatus.RUNTIME_ERROR) {
                        status = "RE";
                    }

                    return SubmissionTestCase.builder()
                            .submission(submission)
                            .testCase(testCase)
                            .status(status)
                            .executionTimeMs(result.getTimeMs())
                            .executionMemoryMb(result.getMemoryMb())
                            .output(result.getOutput())
                            .errorMessage(result.getErrorMessage())
                            .build();
                }))
                .collect(Collectors.toList());

        List<SubmissionTestCase> results = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        // Grade compilation outcomes and performance totals
        int totalScore = 0;
        int maxTime = 0;
        int maxMemory = 0;
        SubmissionStatus overallStatus = SubmissionStatus.ACCEPTED;

        for (SubmissionTestCase tcResult : results) {
            submission.addTestCaseResult(tcResult);
            maxTime = Math.max(maxTime, tcResult.getExecutionTimeMs());
            maxMemory = Math.max(maxMemory, tcResult.getExecutionMemoryMb());

            if ("PASSED".equals(tcResult.getStatus())) {
                totalScore += tcResult.getTestCase().getMarks();
            } else {
                // Map worst test case status to overall status
                if (overallStatus == SubmissionStatus.ACCEPTED || overallStatus == SubmissionStatus.WRONG_ANSWER) {
                    if ("COMPILATION_ERROR".equals(tcResult.getStatus())) {
                        overallStatus = SubmissionStatus.COMPILATION_ERROR;
                    } else if ("TLE".equals(tcResult.getStatus())) {
                        overallStatus = SubmissionStatus.TIME_LIMIT_EXCEEDED;
                    } else if ("MLE".equals(tcResult.getStatus())) {
                        overallStatus = SubmissionStatus.MEMORY_LIMIT_EXCEEDED;
                    } else if ("RE".equals(tcResult.getStatus())) {
                        overallStatus = SubmissionStatus.RUNTIME_ERROR;
                    } else if ("FAILED".equals(tcResult.getStatus())) {
                        overallStatus = SubmissionStatus.WRONG_ANSWER;
                    }
                }
            }
        }

        submission.setStatus(overallStatus);
        submission.setScore(totalScore);
        submission.setExecutionTimeMs(maxTime);
        submission.setExecutionMemoryMb(maxMemory);
    }

    private boolean compareOutputs(String actual, String expected) {
        if (actual == null || expected == null) {
            return false;
        }
        List<String> actualClean = cleanOutputLines(actual);
        List<String> expectedClean = cleanOutputLines(expected);

        if (actualClean.size() != expectedClean.size()) {
            return false;
        }

        for (int i = 0; i < actualClean.size(); i++) {
            if (!actualClean.get(i).equals(expectedClean.get(i))) {
                return false;
            }
        }
        return true;
    }

    private List<String> cleanOutputLines(String output) {
        String[] lines = output.split("\\r?\\n");
        List<String> clean = new ArrayList<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                clean.add(trimmed);
            }
        }
        return clean;
    }

    private Set<String> parseSubmittedOptions(String code) {
        try {
            if (code.trim().startsWith("[")) {
                return OBJECT_MAPPER.readValue(code, new TypeReference<Set<String>>() {});
            }
            return Arrays.stream(code.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Failed to parse MCQ submitted options: {}", code, e);
            return Collections.emptySet();
        }
    }

    private SubmissionResponse toSubmissionResponseSecure(Submission submission) {
        SubmissionResponse response = submissionMapper.toResponse(submission);
        if (response == null || response.getTestCaseResults() == null) {
            return response;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isCandidate = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CANDIDATE"));
        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") ||
                a.getAuthority().equals("ROLE_SUPER_ADMIN") ||
                a.getAuthority().equals("ROLE_EXAM_SETTER")
        );

        // Hide test case parameters from candidate view
        if (isCandidate && !isAdmin) {
            for (SubmissionTestCaseResponse tc : response.getTestCaseResults()) {
                if (tc.isHidden()) {
                    tc.setOutput(null);
                    tc.setErrorMessage("Output hidden for security verification");
                }
            }
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionResponse> getSubmissionsByUser(Long userId) {
        return submissionRepository.findByUserId(userId).stream()
                .map(this::toSubmissionResponseSecure)
                .collect(Collectors.toList());
    }
}
