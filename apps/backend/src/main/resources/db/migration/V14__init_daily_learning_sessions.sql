ALTER TABLE learning_roadmaps
    ADD COLUMN IF NOT EXISTS start_date DATE;

UPDATE learning_roadmaps
SET start_date = created_at::date
WHERE start_date IS NULL;

ALTER TABLE learning_roadmaps
    ALTER COLUMN start_date SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_learning_roadmaps_user_start_date
    ON learning_roadmaps(user_id, start_date);

CREATE TABLE learning_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    roadmap_id BIGINT NOT NULL REFERENCES learning_roadmaps(id) ON DELETE CASCADE,
    roadmap_day_id BIGINT NOT NULL REFERENCES learning_roadmap_days(id) ON DELETE CASCADE,
    session_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED',
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_learning_session_status
        CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED')),
    CONSTRAINT uq_learning_session_user_date
        UNIQUE (user_id, session_date)
);

CREATE INDEX idx_learning_sessions_user_date
    ON learning_sessions(user_id, session_date DESC);

CREATE INDEX idx_learning_sessions_roadmap_day
    ON learning_sessions(roadmap_day_id);

CREATE TABLE learning_session_activities (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL REFERENCES learning_sessions(id) ON DELETE CASCADE,
    roadmap_activity_id BIGINT NOT NULL REFERENCES learning_roadmap_activities(id) ON DELETE CASCADE,
    sequence_no INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED',
    score DOUBLE PRECISION,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    CONSTRAINT ck_learning_session_activity_status
        CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED', 'SKIPPED')),
    CONSTRAINT uq_learning_session_activity_sequence
        UNIQUE (session_id, sequence_no),
    CONSTRAINT uq_learning_session_roadmap_activity
        UNIQUE (session_id, roadmap_activity_id)
);

CREATE INDEX idx_learning_session_activities_session
    ON learning_session_activities(session_id, sequence_no);

CREATE TABLE learning_session_activity_questions (
    id BIGSERIAL PRIMARY KEY,
    activity_id BIGINT NOT NULL REFERENCES learning_session_activities(id) ON DELETE CASCADE,
    question_id BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    order_index INTEGER NOT NULL,
    CONSTRAINT uq_learning_activity_question
        UNIQUE (activity_id, question_id),
    CONSTRAINT uq_learning_activity_question_order
        UNIQUE (activity_id, order_index)
);

CREATE INDEX idx_learning_session_activity_questions_question
    ON learning_session_activity_questions(question_id);

CREATE INDEX idx_learning_session_activity_questions_activity
    ON learning_session_activity_questions(activity_id);

ALTER TABLE submissions
    ADD COLUMN IF NOT EXISTS learning_session_activity_id BIGINT;

ALTER TABLE submissions
    ADD CONSTRAINT fk_submissions_learning_session_activity
    FOREIGN KEY (learning_session_activity_id)
    REFERENCES learning_session_activities(id)
    ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_submissions_learning_session_activity
    ON submissions(learning_session_activity_id);

CREATE INDEX IF NOT EXISTS idx_submissions_user_learning_activity
    ON submissions(user_id, learning_session_activity_id);
