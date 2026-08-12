package com.ajith.codejudge.ai.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class TutorResponseSchema {
    private TutorResponseSchema() {}

    public static ObjectNode buildSchema(ObjectMapper mapper) {
        ObjectNode schema = mapper.createObjectNode();
        schema.put("type", "object");

        ObjectNode properties = schema.putObject("properties");

        ObjectNode reply = properties.putObject("reply");
        reply.put("type", "string");

        ObjectNode diagnosis = properties.putObject("diagnosis");
        diagnosis.put("type", "string");

        ObjectNode nextSteps = properties.putObject("nextSteps");
        nextSteps.put("type", "array");
        ObjectNode nextStepItems = nextSteps.putObject("items");
        nextStepItems.put("type", "string");

        ObjectNode practice = properties.putObject("practiceSuggestion");
        practice.put("type", "string");

        ArrayNode required = schema.putArray("required");
        required.add("reply");
        required.add("diagnosis");
        required.add("nextSteps");
        required.add("practiceSuggestion");

        schema.put("additionalProperties", false);
        return schema;
    }
}
