package com.ajith.codejudge.ai.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

final class McqQuestionSchema {

    private McqQuestionSchema() {
    }

    static ObjectNode buildSchema(ObjectMapper objectMapper) {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");

        ObjectNode properties = schema.putObject("properties");
        stringProperty(properties, "title");
        stringProperty(properties, "description");

        ObjectNode difficulty = properties.putObject("difficulty");
        difficulty.put("type", "string");
        ArrayNode difficultyEnum = difficulty.putArray("enum");
        difficultyEnum.add("EASY");
        difficultyEnum.add("MEDIUM");
        difficultyEnum.add("HARD");

        ObjectNode marks = properties.putObject("marks");
        marks.put("type", "integer");
        marks.put("minimum", 1);
        marks.put("maximum", 100);

        ObjectNode options = properties.putObject("options");
        options.put("type", "array");
        options.put("minItems", 4);
        options.put("maxItems", 4);

        ObjectNode option = options.putObject("items");
        option.put("type", "object");
        ObjectNode optionProperties = option.putObject("properties");
        stringProperty(optionProperties, "id");
        stringProperty(optionProperties, "text");
        optionProperties.putObject("correct").put("type", "boolean");
        ArrayNode optionRequired = option.putArray("required");
        optionRequired.add("id");
        optionRequired.add("text");
        optionRequired.add("correct");
        option.put("additionalProperties", false);

        properties.putObject("multipleChoice").put("type", "boolean");

        ObjectNode negativeMarking = properties.putObject("negativeMarking");
        negativeMarking.put("type", "number");
        negativeMarking.put("minimum", 0);

        properties.putObject("partialMarking").put("type", "boolean");
        properties.putObject("randomizeOptions").put("type", "boolean");
        stringProperty(properties, "explanation");

        ArrayNode required = schema.putArray("required");
        required.add("title");
        required.add("description");
        required.add("difficulty");
        required.add("marks");
        required.add("options");
        required.add("multipleChoice");
        required.add("negativeMarking");
        required.add("partialMarking");
        required.add("randomizeOptions");
        required.add("explanation");

        schema.put("additionalProperties", false);
        return schema;
    }

    private static void stringProperty(ObjectNode properties, String name) {
        properties.putObject(name).put("type", "string");
    }
}
