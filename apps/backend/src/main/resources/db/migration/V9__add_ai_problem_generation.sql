CREATE TABLE ai_problem_generations (
    id BIGSERIAL PRIMARY KEY,
    skill_id BIGINT NOT NULL REFERENCES skills(id) ON DELETE RESTRICT,
    difficulty VARCHAR(20) NOT NULL,
    language_code VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'GENERATING',
    generated_payload TEXT,
    validation_message TEXT,
    question_id BIGINT REFERENCES questions(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    validated_at TIMESTAMP,
    published_at TIMESTAMP,
    CONSTRAINT ck_ai_problem_generation_status
        CHECK (status IN ('GENERATING','VALIDATED','FAILED','PUBLISHED'))
);

CREATE INDEX idx_ai_problem_generations_skill
    ON ai_problem_generations(skill_id);

CREATE INDEX idx_ai_problem_generations_status
    ON ai_problem_generations(status);
