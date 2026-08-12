CREATE TABLE ai_tutor_conversations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    title VARCHAR(255) NOT NULL,
    question_id BIGINT REFERENCES questions(id),
    submission_id BIGINT REFERENCES submissions(id),
    roadmap_id BIGINT REFERENCES learning_roadmaps(id),
    skill_id BIGINT REFERENCES skills(id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_ai_tutor_conversations_user_updated
    ON ai_tutor_conversations(user_id, updated_at DESC);

CREATE INDEX idx_ai_tutor_conversations_question
    ON ai_tutor_conversations(question_id);

CREATE TABLE ai_tutor_messages (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL
        REFERENCES ai_tutor_conversations(id)
        ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT chk_ai_tutor_message_role
        CHECK (role IN ('USER', 'ASSISTANT'))
);

CREATE INDEX idx_ai_tutor_messages_conversation_created
    ON ai_tutor_messages(conversation_id, created_at);
