package com.ajith.codejudge.ai.service;

import com.ajith.codejudge.ai.dto.request.GenerateProblemRequest;
import com.ajith.codejudge.ai.dto.response.ProblemGenerationResponse;
import com.ajith.codejudge.question.dto.response.CodingQuestionResponse;

public interface ProblemGenerationService {

    ProblemGenerationResponse generate(GenerateProblemRequest request);

    CodingQuestionResponse publish(Long generationId);
}
