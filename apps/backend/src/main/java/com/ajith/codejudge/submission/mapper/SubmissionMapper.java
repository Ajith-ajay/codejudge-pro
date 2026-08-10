package com.ajith.codejudge.submission.mapper;

import com.ajith.codejudge.submission.dto.request.SubmissionRequest;
import com.ajith.codejudge.submission.dto.response.SubmissionResponse;
import com.ajith.codejudge.submission.dto.response.SubmissionTestCaseResponse;
import com.ajith.codejudge.submission.entity.Submission;
import com.ajith.codejudge.submission.entity.SubmissionTestCase;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
    componentModel = "spring",
    builder = @org.mapstruct.Builder(disableBuilder = true)
)
public interface SubmissionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "candidate", ignore = true)
    @Mapping(target = "assessment", ignore = true)
    @Mapping(target = "question", ignore = true)
    @Mapping(target = "language", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "executionTimeMs", ignore = true)
    @Mapping(target = "executionMemoryMb", ignore = true)
    @Mapping(target = "score", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "submissionTestCases", ignore = true)
    @Mapping(target = "user", ignore = true)
    Submission toEntity(SubmissionRequest request);

    @Mapping(target = "candidateId", source = "candidate.id")
    @Mapping(target = "assessmentId", source = "assessment.id")
    @Mapping(target = "questionId", source = "question.id")
    @Mapping(target = "questionTitle", source = "question.title")
    @Mapping(target = "languageCode", source = "language.code")
    @Mapping(target = "languageName", source = "language.name")
    @Mapping(target = "testCaseResults", source = "submissionTestCases")
    SubmissionResponse toResponse(Submission submission);

    @Mapping(target = "testCaseId", source = "testCase.id")
    @Mapping(target = "hidden", source = "testCase.hidden")
    SubmissionTestCaseResponse toResponse(SubmissionTestCase testCase);
}
