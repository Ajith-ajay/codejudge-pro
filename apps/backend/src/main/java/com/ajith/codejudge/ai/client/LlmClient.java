package com.ajith.codejudge.ai.client;

public interface LlmClient {

    String generateProblem(String prompt);

    String generateMcq(String prompt);
}
