CREATE TABLE skills (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(50) NOT NULL,
    parent_id BIGINT REFERENCES skills(id) ON DELETE SET NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE question_skills (
    question_id BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    skill_id BIGINT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
    weight NUMERIC(5,2) NOT NULL DEFAULT 1.00,
    PRIMARY KEY (question_id, skill_id)
);

CREATE INDEX idx_question_skills_skill ON question_skills(skill_id);
CREATE INDEX idx_question_skills_question ON question_skills(question_id);

CREATE TABLE user_skill_progress (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    skill_id BIGINT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
    mastery_score DOUBLE PRECISION NOT NULL DEFAULT 0,
    confidence_score DOUBLE PRECISION NOT NULL DEFAULT 0,
    attempts INTEGER NOT NULL DEFAULT 0,
    correct_attempts INTEGER NOT NULL DEFAULT 0,
    coding_attempts INTEGER NOT NULL DEFAULT 0,
    coding_correct INTEGER NOT NULL DEFAULT 0,
    mcq_attempts INTEGER NOT NULL DEFAULT 0,
    mcq_correct INTEGER NOT NULL DEFAULT 0,
    last_attempted_at TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_user_skill_progress UNIQUE(user_id, skill_id),
    CONSTRAINT ck_mastery_score CHECK (mastery_score >= 0 AND mastery_score <= 100),
    CONSTRAINT ck_confidence_score CHECK (confidence_score >= 0 AND confidence_score <= 100)
);

CREATE INDEX idx_user_skill_progress_user ON user_skill_progress(user_id);
CREATE INDEX idx_user_skill_progress_skill ON user_skill_progress(skill_id);

CREATE TABLE learning_assessments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    skill_id BIGINT REFERENCES skills(id) ON DELETE SET NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'GENERATED',
    total_questions INTEGER NOT NULL,
    mcq_count INTEGER NOT NULL,
    coding_count INTEGER NOT NULL,
    target_difficulty VARCHAR(20) NOT NULL,
    score DOUBLE PRECISION,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    CONSTRAINT ck_assessment_status CHECK (status IN ('GENERATED','IN_PROGRESS','COMPLETED'))
);

CREATE TABLE assessment_questions (
    assessment_id BIGINT NOT NULL REFERENCES learning_assessments(id) ON DELETE CASCADE,
    question_id BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    order_index INTEGER NOT NULL,
    PRIMARY KEY (assessment_id, question_id),
    CONSTRAINT uq_assessment_question_order UNIQUE(assessment_id, order_index)
);

CREATE INDEX idx_assessment_questions_question ON assessment_questions(question_id);
CREATE INDEX idx_learning_assessments_user ON learning_assessments(user_id);

-- Initial placement-preparation skill taxonomy.
INSERT INTO skills (name, category) VALUES
('Arrays', 'DSA'),
('Strings', 'DSA'),
('Hashing', 'DSA'),
('Two Pointers', 'DSA'),
('Sliding Window', 'DSA'),
('Binary Search', 'DSA'),
('Linked List', 'DSA'),
('Stack', 'DSA'),
('Queue', 'DSA'),
('Trees', 'DSA'),
('Binary Search Tree', 'DSA'),
('Heap', 'DSA'),
('Graphs', 'DSA'),
('BFS', 'DSA'),
('DFS', 'DSA'),
('Recursion', 'DSA'),
('Backtracking', 'DSA'),
('Greedy', 'DSA'),
('Dynamic Programming', 'DSA'),
('Sorting', 'DSA'),
('Time Complexity', 'DSA'),
('Space Complexity', 'DSA'),
('OOP', 'CORE'),
('DBMS', 'CORE'),
('SQL', 'CORE'),
('Operating Systems', 'CORE'),
('Computer Networks', 'CORE'),
('Aptitude', 'APTITUDE'),
('Logical Reasoning', 'APTITUDE'),
('Java', 'PROGRAMMING'),
('Python', 'PROGRAMMING');
