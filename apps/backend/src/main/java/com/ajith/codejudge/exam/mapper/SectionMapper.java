package com.ajith.codejudge.exam.mapper;

import com.ajith.codejudge.exam.dto.request.SectionRequest;
import com.ajith.codejudge.exam.dto.response.SectionQuestionResponse;
import com.ajith.codejudge.exam.dto.response.SectionResponse;
import com.ajith.codejudge.exam.entity.Section;
import com.ajith.codejudge.exam.entity.SectionQuestion;
import com.ajith.codejudge.question.mapper.QuestionMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
    componentModel = "spring",
    uses = {QuestionMapper.class},
    builder = @org.mapstruct.Builder(disableBuilder = true)
)
public interface SectionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "exam", ignore = true)
    @Mapping(target = "sectionQuestions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Section toEntity(SectionRequest request);

    @Mapping(target = "questions", source = "sectionQuestions")
    SectionResponse toResponse(Section section);

    @Mapping(target = "question", source = "question")
    @Mapping(target = "orderIndex", source = "orderIndex")
    SectionQuestionResponse toSectionQuestionResponse(SectionQuestion sectionQuestion);
}
