package com.ajith.codejudge.exam.service.impl;

import com.ajith.codejudge.common.pagination.PageRequestDto;
import com.ajith.codejudge.common.pagination.PageResponseDto;
import com.ajith.codejudge.exam.dto.request.CandidateInviteRequest;
import com.ajith.codejudge.exam.dto.request.ExamRequest;
import com.ajith.codejudge.exam.dto.response.ExamCandidateResponse;
import com.ajith.codejudge.exam.dto.response.ExamResponse;
import com.ajith.codejudge.exam.entity.CandidateStatus;
import com.ajith.codejudge.exam.entity.Exam;
import com.ajith.codejudge.exam.entity.ExamCandidate;
import com.ajith.codejudge.exam.entity.Section;
import com.ajith.codejudge.exam.entity.SectionQuestion;
import com.ajith.codejudge.exam.mapper.ExamCandidateMapper;
import com.ajith.codejudge.exam.mapper.ExamMapper;
import com.ajith.codejudge.exam.mapper.SectionMapper;
import com.ajith.codejudge.exam.repository.ExamCandidateRepository;
import com.ajith.codejudge.exam.repository.ExamRepository;
import com.ajith.codejudge.exam.repository.SectionQuestionRepository;
import com.ajith.codejudge.exam.repository.SectionRepository;
import com.ajith.codejudge.exam.service.interfaces.ExamService;
import com.ajith.codejudge.exception.BadRequestException;
import com.ajith.codejudge.exception.ConflictException;
import com.ajith.codejudge.exception.ForbiddenException;
import com.ajith.codejudge.exception.ResourceNotFoundException;
import com.ajith.codejudge.notification.service.interfaces.EmailService;
import com.ajith.codejudge.question.entity.Question;
import com.ajith.codejudge.question.repository.QuestionRepository;
import com.ajith.codejudge.user.entity.User;
import com.ajith.codejudge.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {

    private final ExamRepository examRepository;
    private final SectionRepository sectionRepository;
    private final SectionQuestionRepository sectionQuestionRepository;
    private final ExamCandidateRepository examCandidateRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    private final ExamMapper examMapper;
    private final SectionMapper sectionMapper;
    private final ExamCandidateMapper examCandidateMapper;
    private final EmailService emailService;

    @Override
    @Transactional
    public ExamResponse createExam(ExamRequest request) {
        validateExamTimings(request.getStartTime(), request.getEndTime());

        Exam exam = examMapper.toEntity(request);
        exam = examRepository.save(exam);

        if (request.getSections() != null) {
            for (var secRequest : request.getSections()) {
                Section section = sectionMapper.toEntity(secRequest);
                section.setExam(exam);
                section = sectionRepository.save(section);

                if (secRequest.getQuestionIds() != null) {
                    int orderIndex = 0;
                    for (Long qId : secRequest.getQuestionIds()) {
                        Question question = questionRepository.findById(qId)
                                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + qId));

                        SectionQuestion sectionQuestion = SectionQuestion.builder()
                                .section(section)
                                .question(question)
                                .orderIndex(orderIndex++)
                                .build();
                        sectionQuestionRepository.save(sectionQuestion);
                    }
                }
            }
        }

        log.info("Successfully created scheduled exam: {}", exam.getTitle());
        return getExamById(exam.getId());
    }

    @Override
    @Transactional
    public ExamResponse updateExam(Long id, ExamRequest request) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + id));

        if (exam.isPublished() && !exam.isClosed()) {
            // If already published, check if we can edit
            log.warn("Editing a published exam with id: {}", id);
        }

        validateExamTimings(request.getStartTime(), request.getEndTime());
        examMapper.updateEntity(request, exam);
        exam = examRepository.save(exam);

        // Delete old sections and clean rebuild
        List<Section> oldSections = sectionRepository.findByExamIdOrderByOrderIndexAsc(id);
        sectionRepository.deleteAll(oldSections);

        if (request.getSections() != null) {
            for (var secRequest : request.getSections()) {
                Section section = sectionMapper.toEntity(secRequest);
                section.setExam(exam);
                section = sectionRepository.save(section);

                if (secRequest.getQuestionIds() != null) {
                    int orderIndex = 0;
                    for (Long qId : secRequest.getQuestionIds()) {
                        Question question = questionRepository.findById(qId)
                                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + qId));

                        SectionQuestion sectionQuestion = SectionQuestion.builder()
                                .section(section)
                                .question(question)
                                .orderIndex(orderIndex++)
                                .build();
                        sectionQuestionRepository.save(sectionQuestion);
                    }
                }
            }
        }

        log.info("Successfully updated exam with id: {}", id);
        return getExamById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public ExamResponse getExamById(Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + id));
        return examMapper.toResponse(exam);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<ExamResponse> getAllExams(PageRequestDto pageRequest) {
        Pageable pageable = pageRequest.toPageable();
        Page<Exam> exams = examRepository.findAll(pageable);
        Page<ExamResponse> responsePage = exams.map(examMapper::toResponse);
        return PageResponseDto.fromPage(responsePage);
    }

    @Override
    @Transactional
    public void deleteExam(Long id) {
        if (!examRepository.existsById(id)) {
            throw new ResourceNotFoundException("Exam not found with id: " + id);
        }
        examRepository.deleteById(id);
        log.info("Deleted exam with id: {}", id);
    }

    @Override
    @Transactional
    public ExamResponse publishExam(Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + id));
        if (exam.isPublished()) {
            throw new ConflictException("Exam is already published");
        }
        exam.setPublished(true);
        exam = examRepository.save(exam);
        log.info("Published exam: {}", exam.getTitle());
        return examMapper.toResponse(exam);
    }

    @Override
    @Transactional
    public ExamResponse closeExam(Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + id));
        if (exam.isClosed()) {
            throw new ConflictException("Exam is already closed");
        }
        exam.setClosed(true);
        exam = examRepository.save(exam);
        log.info("Closed exam: {}", exam.getTitle());
        return examMapper.toResponse(exam);
    }

    @Override
    @Transactional
    public List<ExamCandidateResponse> inviteCandidates(Long examId, CandidateInviteRequest request) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + examId));

        List<ExamCandidateResponse> responses = new ArrayList<>();
        for (String email : request.getEmails()) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Registered user not found with email: " + email));

            if (examCandidateRepository.existsByExamIdAndUserId(examId, user.getId())) {
                log.info("Candidate {} already invited to exam {}", email, exam.getTitle());
                ExamCandidate candidate = examCandidateRepository.findByExamIdAndUserId(examId, user.getId()).orElseThrow();
                responses.add(examCandidateMapper.toResponse(candidate));
                continue;
            }

            ExamCandidate candidate = ExamCandidate.builder()
                    .exam(exam)
                    .user(user)
                    .status(CandidateStatus.INVITED)
                    .invitedAt(LocalDateTime.now())
                    .build();
            candidate = examCandidateRepository.save(candidate);

            // Send notification email
            emailService.sendExamInvitationEmail(
                    user.getEmail(),
                    exam.getTitle(),
                    exam.getStartTime().toString(),
                    String.valueOf(exam.getDurationMinutes())
            );

            responses.add(examCandidateMapper.toResponse(candidate));
        }

        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamCandidateResponse> getExamCandidates(Long examId) {
        if (!examRepository.existsById(examId)) {
            throw new ResourceNotFoundException("Exam not found with id: " + examId);
        }
        return examCandidateRepository.findByExamId(examId).stream()
                .map(examCandidateMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamResponse> getActiveExamsForCandidate(Long userId) {
        List<ExamCandidate> candidates = examCandidateRepository.findByUserId(userId);
        return candidates.stream()
                .map(ExamCandidate::getExam)
                .filter(exam -> exam.isPublished() && !exam.isClosed())
                .map(examMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ExamCandidateResponse startExam(Long examId, Long userId) {
        ExamCandidate candidate = examCandidateRepository.findByExamIdAndUserId(examId, userId)
                .orElseThrow(() -> new ForbiddenException("You are not invited to participate in this exam"));

        Exam exam = candidate.getExam();
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(exam.getStartTime()) || now.isAfter(exam.getEndTime())) {
            throw new BadRequestException("The exam session window is not currently active");
        }

        if (candidate.getStatus() == CandidateStatus.COMPLETED) {
            throw new ConflictException("You have already completed this exam session");
        }

        if (candidate.getStatus() == CandidateStatus.INVITED) {
            candidate.setStatus(CandidateStatus.STARTED);
            candidate.setJoinedAt(now);
            candidate.setStartedAt(now);
            candidate = examCandidateRepository.save(candidate);
            log.info("Candidate {} started exam {}", candidate.getUser().getUsername(), exam.getTitle());
        }

        return examCandidateMapper.toResponse(candidate);
    }

    @Override
    @Transactional
    public ExamCandidateResponse completeExam(Long examId, Long userId) {
        ExamCandidate candidate = examCandidateRepository.findByExamIdAndUserId(examId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam attempt record not found"));

        if (candidate.getStatus() != CandidateStatus.COMPLETED) {
            candidate.setStatus(CandidateStatus.COMPLETED);
            candidate.setCompletedAt(LocalDateTime.now());
            candidate = examCandidateRepository.save(candidate);
            log.info("Candidate {} completed exam {}", candidate.getUser().getUsername(), candidate.getExam().getTitle());
        }

        return examCandidateMapper.toResponse(candidate);
    }

    private void validateExamTimings(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new BadRequestException("Start time and end time are required");
        }
        if (start.isAfter(end) || start.isEqual(end)) {
            throw new BadRequestException("Exam start time must be chronologically before the end time");
        }
    }
}
