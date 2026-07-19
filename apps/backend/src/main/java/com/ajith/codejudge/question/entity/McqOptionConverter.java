package com.ajith.codejudge.question.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Converter
public class McqOptionConverter implements AttributeConverter<List<McqOption>, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<McqOption> attribute) {
        if (attribute == null) {
            return "[]";
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            log.error("Error converting list of options to JSON string", e);
            return "[]";
        }
    }

    @Override
    public List<McqOption> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return OBJECT_MAPPER.readValue(dbData, new TypeReference<List<McqOption>>() {});
        } catch (IOException e) {
            log.error("Error converting JSON string to list of options", e);
            return Collections.emptyList();
        }
    }
}
