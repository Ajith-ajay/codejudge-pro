package com.ajith.codejudge.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {

    /**
     * Provider name used for logging/diagnostics only.
     * Examples: openai, ollama, groq, openrouter, lmstudio, custom.
     */
    private String provider = "openai";

    /**
     * Protocol used by the configured endpoint.
     *
     * OPENAI_CHAT:
     *   OpenAI-compatible POST /chat/completions
     *
     * OPENAI_RESPONSES:
     *   OpenAI-compatible POST /responses
     */
    private Protocol protocol = Protocol.OPENAI_CHAT;

    private String apiKey = "";
    private String baseUrl = "https://api.openai.com/v1";
    private String model = "gpt-5";
    private long timeoutSeconds = 90;
    private long connectTimeoutSeconds = 15;

    /**
     * Whether to request strict JSON Schema output.
     * Disable this for providers/models that do not support JSON Schema.
     */
    private boolean structuredOutput = true;

    /**
     * Output mode when structuredOutput is false.
     * JSON_OBJECT asks the model for valid JSON without a schema.
     */
    private OutputMode outputMode = OutputMode.JSON_OBJECT;

    /**
     * Optional provider-specific HTTP headers.
     * Example:
     * headers:
     *   HTTP-Referer: https://your-app.example
     *   X-Title: CodeJudgePro
     */
    private Map<String, String> headers = new HashMap<>();

    public enum Protocol {
        OPENAI_CHAT,
        OPENAI_RESPONSES
    }

    public enum OutputMode {
        JSON_SCHEMA,
        JSON_OBJECT,
        TEXT
    }
}
