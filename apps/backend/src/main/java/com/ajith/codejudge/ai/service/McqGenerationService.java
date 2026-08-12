package com.ajith.codejudge.ai.service;

import com.ajith.codejudge.ai.dto.request.GenerateMcqRequest;
import com.ajith.codejudge.ai.dto.response.McqGenerationResponse;
import com.ajith.codejudge.question.dto.response.McqQuestionResponse;

public interface McqGenerationService {
    McqGenerationResponse generate(GenerateMcqRequest request);
    McqQuestionResponse publish(Long generationId);
}
