package com.ajith.codejudge.question.mapper;

import com.ajith.codejudge.question.dto.request.TestCaseRequest;
import com.ajith.codejudge.question.dto.response.TestCaseResponse;
import com.ajith.codejudge.question.entity.TestCase;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TestCaseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codingQuestion", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TestCase toEntity(TestCaseRequest request);

    TestCaseResponse toResponse(TestCase testCase);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codingQuestion", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(TestCaseRequest request, @MappingTarget TestCase testCase);
}
