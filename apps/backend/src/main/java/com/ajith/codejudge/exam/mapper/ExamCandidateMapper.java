package com.ajith.codejudge.exam.mapper;

import com.ajith.codejudge.exam.dto.response.ExamCandidateResponse;
import com.ajith.codejudge.exam.entity.ExamCandidate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
    componentModel = "spring",
    builder = @org.mapstruct.Builder(disableBuilder = true)
)
public interface ExamCandidateMapper {

    @Mapping(target = "examId", source = "exam.id")
    @Mapping(target = "examTitle", source = "exam.title")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "email", source = "user.email")
    ExamCandidateResponse toResponse(ExamCandidate candidate);
}
