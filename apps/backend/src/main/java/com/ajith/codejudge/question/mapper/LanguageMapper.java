package com.ajith.codejudge.question.mapper;

import com.ajith.codejudge.question.dto.request.LanguageRequest;
import com.ajith.codejudge.question.dto.response.LanguageResponse;
import com.ajith.codejudge.question.entity.Language;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface LanguageMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Language toEntity(LanguageRequest request);

    LanguageResponse toResponse(Language language);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(LanguageRequest request, @MappingTarget Language language);
}
