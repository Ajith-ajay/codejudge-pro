package com.ajith.codejudge.ai.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

final class LearningRoadmapSchema {

    private LearningRoadmapSchema() {
    }

    static ObjectNode buildSchema(ObjectMapper mapper) {
        ObjectNode schema = mapper.createObjectNode();
        schema.put("type", "object");

        ObjectNode properties = schema.putObject("properties");
        string(properties, "title");
        string(properties, "summary");

        ObjectNode weeks = properties.putObject("weeks");
        weeks.put("type", "array");
        weeks.put("minItems", 1);
        weeks.put("maxItems", 26);

        ObjectNode week = weeks.putObject("items");
        week.put("type", "object");
        ObjectNode wp = week.putObject("properties");
        integer(wp, "weekNumber");
        string(wp, "objective");

        ObjectNode days = wp.putObject("days");
        days.put("type", "array");
        days.put("minItems", 1);
        days.put("maxItems", 7);
        ObjectNode day = days.putObject("items");
        day.put("type", "object");
        ObjectNode dp = day.putObject("properties");
        integer(dp, "dayNumber");
        string(dp, "title");
        string(dp, "focus");

        ObjectNode activities = dp.putObject("activities");
        activities.put("type", "array");
        activities.put("minItems", 1);
        activities.put("maxItems", 6);
        ObjectNode activity = activities.putObject("items");
        activity.put("type", "object");
        ObjectNode ap = activity.putObject("properties");
        stringEnum(ap, "type", "LEARN", "MCQ", "CODING", "REVIEW", "MOCK_TEST");
        string(ap, "instructions");
        integer(ap, "estimatedMinutes");
        ObjectNode skillIds = ap.putObject("skillIds");
        skillIds.put("type", "array");
        skillIds.put("minItems", 1);
        skillIds.put("maxItems", 4);
        skillIds.set("items", mapper.createObjectNode().put("type", "integer"));

        required(activity, "type", "instructions", "estimatedMinutes", "skillIds");
        activity.put("additionalProperties", false);
        required(day, "dayNumber", "title", "focus", "activities");
        day.put("additionalProperties", false);
        required(week, "weekNumber", "objective", "days");
        week.put("additionalProperties", false);

        required(schema, "title", "summary", "weeks");
        schema.put("additionalProperties", false);
        return schema;
    }

    private static void string(ObjectNode p, String name) {
        p.putObject(name).put("type", "string");
    }

    private static void integer(ObjectNode p, String name) {
        p.putObject(name).put("type", "integer");
    }

    private static void stringEnum(ObjectNode p, String name, String... values) {
        ObjectNode node = p.putObject(name);
        node.put("type", "string");
        ArrayNode array = node.putArray("enum");
        for (String value : values) array.add(value);
    }

    private static void required(ObjectNode node, String... names) {
        ArrayNode required = node.putArray("required");
        for (String name : names) required.add(name);
    }
}
