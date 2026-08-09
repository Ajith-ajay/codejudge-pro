package com.ajith.codejudge.ai.client;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

final class CodingProblemSchema {

    private CodingProblemSchema() {
    }

    static ObjectNode buildSchema(com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
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

        stringProperty(properties, "constraints");
        ObjectNode timeLimit = properties.putObject("timeLimitMs");
        timeLimit.put("type", "integer");
        timeLimit.put("minimum", 1000);
        timeLimit.put("maximum", 10000);

        ObjectNode memoryLimit = properties.putObject("memoryLimitMb");
        memoryLimit.put("type", "integer");
        memoryLimit.put("minimum", 128);
        memoryLimit.put("maximum", 1024);

        ObjectNode testCases = properties.putObject("testCases");
        testCases.put("type", "array");
        testCases.put("minItems", 4);
        testCases.put("maxItems", 10);
        ObjectNode items = testCases.putObject("items");
        items.put("type", "object");
        items.set("properties", testCaseProperties(objectMapper));
        ArrayNode testRequired = items.putArray("required");
        testRequired.add("input");
        testRequired.add("hidden");
        testRequired.add("marks");
        items.put("additionalProperties", false);

        ObjectNode contains = testCases.putObject("contains");
        contains.put("type", "object");

        ObjectNode containsProperties = contains.putObject("properties");

        ObjectNode hiddenProperty = containsProperties.putObject("hidden");
        hiddenProperty.put("const", true);

        ArrayNode containsRequired = contains.putArray("required");
        containsRequired.add("hidden");

        contains.put("additionalProperties", false);

        stringProperty(properties, "referenceSolution");

        ArrayNode required = schema.putArray("required");
        required.add("title");
        required.add("description");
        required.add("difficulty");
        required.add("constraints");
        required.add("timeLimitMs");
        required.add("memoryLimitMb");
        required.add("testCases");
        required.add("referenceSolution");

        schema.put("additionalProperties", false);
        return schema;
    }

    private static ObjectNode testCaseProperties(com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        ObjectNode properties = objectMapper.createObjectNode();
        stringProperty(properties, "input");
        properties.putObject("hidden").put("type", "boolean");
        integerProperty(properties, "marks");
        return properties;
    }

    private static void stringProperty(ObjectNode properties, String name) {
        properties.putObject(name).put("type", "string");
    }

    private static void integerProperty(ObjectNode properties, String name) {
        properties.putObject(name).put("type", "integer");
    }
}
