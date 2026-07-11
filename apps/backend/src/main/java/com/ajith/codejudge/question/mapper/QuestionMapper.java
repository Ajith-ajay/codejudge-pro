package com.ajith.codejudge.question.mapper;

import com.ajith.codejudge.question.dto.request.CodingQuestionRequest;
import com.ajith.codejudge.question.dto.request.McqQuestionRequest;
import com.ajith.codejudge.question.dto.response.CodingQuestionResponse;
import com.ajith.codejudge.question.dto.response.McqQuestionResponse;
import com.ajith.codejudge.question.dto.response.QuestionResponse;
import com.ajith.codejudge.question.entity.CodingQuestion;
import com.ajith.codejudge.question.entity.McqQuestion;
import com.ajith.codejudge.question.entity.Question;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
    componentModel = "spring",
    uses = {TestCaseMapper.class, LanguageMapper.class},
    builder = @org.mapstruct.Builder(disableBuilder = true)
)
public interface QuestionMapper {

    // MCQ Question mappings
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", constant = "MCQ")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    McqQuestion toEntity(McqQuestionRequest request);

    McqQuestionResponse toResponse(McqQuestion mcqQuestion);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(McqQuestionRequest request, @MappingTarget McqQuestion mcqQuestion);

    // Coding Question mappings
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", constant = "CODING")
    @Mapping(target = "allowedLanguages", ignore = true)
    @Mapping(target = "testCases", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    CodingQuestion toEntity(CodingQuestionRequest request);

    @Mapping(target = "allowedLanguages", source = "allowedLanguages")
    CodingQuestionResponse toResponse(CodingQuestion codingQuestion);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "allowedLanguages", ignore = true)
    @Mapping(target = "testCases", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(CodingQuestionRequest request, @MappingTarget CodingQuestion codingQuestion);

    default QuestionResponse toResponse(Question question) {
        if (question == null) {
            return null;
        }
        if (question instanceof McqQuestion mcqQuestion) {
            return toResponse(mcqQuestion);
        } else if (question instanceof CodingQuestion codingQuestion) {
            return toResponse(codingQuestion);
        }
        throw new IllegalArgumentException("Unknown Question type: " + question.getClass().getName());
    }
}
