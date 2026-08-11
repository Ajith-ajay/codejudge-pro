CREATE TABLE learning_roadmaps (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    summary TEXT NOT NULL,
    goal VARCHAR(255) NOT NULL,
    target_role VARCHAR(100) NOT NULL,
    preferred_language VARCHAR(30) NOT NULL,
    duration_days INTEGER NOT NULL,
    daily_minutes INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    version INTEGER NOT NULL DEFAULT 1,
    generated_payload JSONB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_learning_roadmap_status CHECK (status IN ('ACTIVE','ARCHIVED','COMPLETED')),
    CONSTRAINT ck_learning_roadmap_duration CHECK (duration_days BETWEEN 1 AND 180),
    CONSTRAINT ck_learning_roadmap_daily_minutes CHECK (daily_minutes BETWEEN 15 AND 480)
);

CREATE UNIQUE INDEX uq_active_learning_roadmap_user
    ON learning_roadmaps(user_id)
    WHERE status = 'ACTIVE';

CREATE INDEX idx_learning_roadmaps_user_created
    ON learning_roadmaps(user_id, created_at DESC);

CREATE TABLE learning_roadmap_days (
    id BIGSERIAL PRIMARY KEY,
    roadmap_id BIGINT NOT NULL REFERENCES learning_roadmaps(id) ON DELETE CASCADE,
    week_number INTEGER NOT NULL,
    day_number INTEGER NOT NULL,
    title VARCHAR(255) NOT NULL,
    focus TEXT NOT NULL,
    UNIQUE (roadmap_id, day_number),
    UNIQUE (roadmap_id, week_number, day_number)
);

CREATE INDEX idx_learning_roadmap_days_roadmap
    ON learning_roadmap_days(roadmap_id, day_number);

CREATE TABLE learning_roadmap_activities (
    id BIGSERIAL PRIMARY KEY,
    day_id BIGINT NOT NULL REFERENCES learning_roadmap_days(id) ON DELETE CASCADE,
    sequence_no INTEGER NOT NULL,
    activity_type VARCHAR(20) NOT NULL,
    instructions TEXT NOT NULL,
    estimated_minutes INTEGER NOT NULL,
    UNIQUE (day_id, sequence_no),
    CONSTRAINT ck_roadmap_activity_type CHECK (activity_type IN ('LEARN','MCQ','CODING','REVIEW','MOCK_TEST')),
    CONSTRAINT ck_roadmap_activity_minutes CHECK (estimated_minutes BETWEEN 1 AND 480)
);

CREATE INDEX idx_learning_roadmap_activities_day
    ON learning_roadmap_activities(day_id, sequence_no);
CREATE TABLE learning_roadmap_activity_skills (
    activity_id BIGINT NOT NULL REFERENCES learning_roadmap_activities(id) ON DELETE CASCADE,
    skill_id BIGINT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
    PRIMARY KEY (activity_id, skill_id)
);

CREATE INDEX idx_learning_roadmap_activity_skills_skill
    ON learning_roadmap_activity_skills(skill_id);
