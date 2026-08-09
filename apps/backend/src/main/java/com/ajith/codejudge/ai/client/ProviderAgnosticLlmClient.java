package com.ajith.codejudge.ai.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.ajith.codejudge.ai.config.AiProperties;
import com.ajith.codejudge.exception.AiServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Provider-agnostic LLM client for OpenAI-compatible APIs.
 *
 * This covers OpenAI, Ollama, Groq, OpenRouter, LM Studio, vLLM, Together, and
 * other providers exposing the OpenAI Chat Completions or Responses API.
 *
 * The application does not need to know which provider is being used; provider
 * details live entirely in application.yml/environment variables.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProviderAgnosticLlmClient implements LlmClient {

    private final ObjectMapper objectMapper;
    private final AiProperties properties;

    private HttpClient httpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(properties.getConnectTimeoutSeconds()))
                .build();
    }

    @Override
    public String generateProblem(String prompt) {
        validateConfiguration();

        try {
            ObjectNode request = buildRequest(prompt);

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint()))
                    .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            objectMapper.writeValueAsString(request)
                    ));

            addAuthentication(builder);
            addCustomHeaders(builder);

            log.debug(
                    "Calling LLM provider={} protocol={} model={} endpoint={}",
                    properties.getProvider(),
                    properties.getProtocol(),
                    properties.getModel(),
                    endpoint()
            );

            HttpResponse<String> response = httpClient().send(
                    builder.build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.error(
                        "LLM provider failed. provider={}, status={}, body={}",
                        properties.getProvider(),
                        response.statusCode(),
                        truncate(response.body(), 2000)
                );
                throw new AiServiceException(
                        "LLM provider '" + properties.getProvider()
                        + "' returned HTTP " + response.statusCode()
                );
            }

            JsonNode root = objectMapper.readTree(response.body());
            String output = extractText(root);

            if (output == null || output.isBlank()) {
                throw new AiServiceException(
                        "LLM provider '" + properties.getProvider()
                        + "' returned an empty problem response"
                );
            }

            return output;
        } catch (AiServiceException ex) {
            throw ex;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AiServiceException("LLM request was interrupted", ex);
        } catch (Exception ex) {
            log.error(
                    "LLM request failed. provider={}, protocol={}, baseUrl={}, model={}, timeout={}s, error={}",
                    properties.getProvider(),
                    properties.getProtocol(),
                    properties.getBaseUrl(),
                    properties.getModel(),
                    properties.getTimeoutSeconds(),
                    ex
            );
            throw new AiServiceException(
                    "Failed to communicate with LLM provider '" + properties.getProvider() + "'",
                    ex
            );
        }
    }

    private void validateConfiguration() {
        if (properties.getBaseUrl() == null || properties.getBaseUrl().isBlank()) {
            throw new AiServiceException("AI base URL is not configured");
        }
        if (properties.getModel() == null || properties.getModel().isBlank()) {
            throw new AiServiceException("AI model is not configured");
        }
        if (properties.getTimeoutSeconds() <= 0) {
            throw new AiServiceException("AI timeout-seconds must be greater than zero");
        }

        // Ollama and some local servers do not require a key.
        if (!isLocalProvider() && isBlank(properties.getApiKey())) {
            log.warn(
                    "No API key configured for provider={}. This is valid only if the endpoint is unauthenticated.",
                    properties.getProvider()
            );
        }
    }

    private ObjectNode buildRequest(String prompt) {
        if (properties.getProtocol() == AiProperties.Protocol.OPENAI_RESPONSES) {
            return buildResponsesRequest(prompt);
        }
        return buildChatRequest(prompt);
    }

    private ObjectNode buildChatRequest(String prompt) {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("model", properties.getModel());

        ArrayNode messages = request.putArray("messages");
        ObjectNode message = messages.addObject();
        message.put("role", "user");
        message.put("content", prompt);

        if (properties.isStructuredOutput()) {
            addJsonSchemaResponseFormat(request);
        } else {
            switch (properties.getOutputMode()) {
                case JSON_OBJECT -> {
                    ObjectNode responseFormat = request.putObject("response_format");
                    responseFormat.put("type", "json_object");
                }
                case JSON_SCHEMA, TEXT -> {
                    // JSON_SCHEMA is intentionally ignored when structured-output is disabled.
                    // TEXT sends no response_format.
                }
            }
        }

        return request;
    }

    private ObjectNode buildResponsesRequest(String prompt) {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("model", properties.getModel());
        request.put("store", false);
        request.put("input", prompt);

        if (properties.isStructuredOutput()) {
            ObjectNode text = request.putObject("text");
            ObjectNode format = text.putObject("format");
            format.put("type", "json_schema");
            format.put("name", "coding_problem");
            format.put("strict", true);
            format.set(
                    "schema",
                    CodingProblemSchema.buildSchema(objectMapper)
            );
        }

        return request;
    }

    private void addJsonSchemaResponseFormat(ObjectNode request) {
        ObjectNode responseFormat = request.putObject("response_format");
        responseFormat.put("type", "json_schema");

        ObjectNode jsonSchema = responseFormat.putObject("json_schema");
        jsonSchema.put("name", "coding_problem");
        jsonSchema.put("strict", true);
        jsonSchema.set(
                "schema",
                CodingProblemSchema.buildSchema(objectMapper)
        );
    }

    private String endpoint() {
        String base = properties.getBaseUrl().trim();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }

        if (properties.getProtocol() == AiProperties.Protocol.OPENAI_RESPONSES) {
            if (base.endsWith("/responses")) {
                return base;
            }
            return base + "/responses";
        }

        if (base.endsWith("/chat/completions")) {
            return base;
        }
        return base + "/chat/completions";
    }

    private void addAuthentication(HttpRequest.Builder builder) {
        if (!isBlank(properties.getApiKey())) {
            builder.header("Authorization", "Bearer " + properties.getApiKey());
        }
    }

    private void addCustomHeaders(HttpRequest.Builder builder) {
        for (Map.Entry<String, String> entry : properties.getHeaders().entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank()
                    || entry.getValue() == null) {
                continue;
            }
            builder.header(entry.getKey(), entry.getValue());
        }
    }

    private String extractText(JsonNode root) {
        if (properties.getProtocol() == AiProperties.Protocol.OPENAI_RESPONSES) {
            String responseText = extractResponsesText(root);
            if (!isBlank(responseText)) {
                return responseText;
            }
        }

        // OpenAI-compatible Chat Completions:
        // choices[0].message.content
        JsonNode choices = root.path("choices");
        if (choices.isArray() && !choices.isEmpty()) {
            JsonNode content = choices.get(0).path("message").path("content");
            if (content.isTextual()) {
                return content.asText();
            }

            // Some providers return content as an array of typed parts.
            if (content.isArray()) {
                String fromParts = extractTextParts(content);
                if (!isBlank(fromParts)) {
                    return fromParts;
                }
            }
        }

        // Some compatible providers return output_text directly.
        JsonNode outputText = root.get("output_text");
        if (outputText != null && outputText.isTextual()) {
            String generatedJson = outputText.asText();

            log.info("========== AI GENERATED PROBLEM ==========");
            log.info("Provider: {}", properties.getProvider());
            log.info("Model: {}", properties.getModel());
            log.info("Generated problem:\n{}", generatedJson);
            log.info("==========================================");

            return generatedJson;
        }

        return null;
    }

    private String extractResponsesText(JsonNode root) {
        JsonNode output = root.path("output");
        if (!output.isArray()) {
            return null;
        }

        StringBuilder result = new StringBuilder();

        for (JsonNode item : output) {
            if (!"message".equals(item.path("type").asText())) {
                continue;
            }

            JsonNode content = item.path("content");
            if (!content.isArray()) {
                continue;
            }

            for (JsonNode part : content) {
                if ("output_text".equals(part.path("type").asText())
                        && part.has("text")) {
                    if (result.length() > 0) {
                        result.append('\n');
                    }
                    result.append(part.get("text").asText());
                }
            }
        }

        return result.isEmpty() ? null : result.toString();
    }

    private String extractTextParts(JsonNode parts) {
        StringBuilder result = new StringBuilder();

        for (JsonNode part : parts) {
            JsonNode text = part.get("text");
            if (text != null && text.isTextual()) {
                if (result.length() > 0) {
                    result.append('\n');
                }
                result.append(text.asText());
            }
        }

        return result.isEmpty() ? null : result.toString();
    }

    private boolean isLocalProvider() {
        String provider = properties.getProvider() == null
                ? ""
                : properties.getProvider().toLowerCase();

        String url = properties.getBaseUrl() == null
                ? ""
                : properties.getBaseUrl().toLowerCase();

        return provider.equals("ollama")
                || url.contains("localhost")
                || url.contains("127.0.0.1")
                || url.contains("0.0.0.0");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        return value.length() <= maxLength
                ? value
                : value.substring(0, maxLength) + "...";
    }
}
