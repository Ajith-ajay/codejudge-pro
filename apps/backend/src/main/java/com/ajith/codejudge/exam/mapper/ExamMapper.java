package com.ajith.codejudge.exam.mapper;

import com.ajith.codejudge.exam.dto.request.ExamRequest;
import com.ajith.codejudge.exam.dto.response.ExamResponse;
import com.ajith.codejudge.exam.entity.Exam;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
    componentModel = "spring",
    uses = {SectionMapper.class},
    builder = @org.mapstruct.Builder(disableBuilder = true)
)
public interface ExamMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "published", ignore = true)
    @Mapping(target = "closed", ignore = true)
    @Mapping(target = "sections", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Exam toEntity(ExamRequest request);

    ExamResponse toResponse(Exam exam);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "published", ignore = true)
    @Mapping(target = "closed", ignore = true)
    @Mapping(target = "sections", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(ExamRequest request, @MappingTarget Exam exam);
}
