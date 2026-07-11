-- Create submissions table
CREATE TABLE submissions (
    id BIGSERIAL PRIMARY KEY,
    candidate_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    language_id BIGINT NOT NULL,
    source_code TEXT NOT NULL,
    status VARCHAR(30) DEFAULT 'PENDING' NOT NULL,
    execution_time_ms INTEGER,
    execution_memory_mb INTEGER,
    score INTEGER DEFAULT 0 NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_submissions_candidate FOREIGN KEY (candidate_id) REFERENCES exam_candidates (id) ON DELETE CASCADE,
    CONSTRAINT fk_submissions_question FOREIGN KEY (question_id) REFERENCES questions (id) ON DELETE CASCADE,
    CONSTRAINT fk_submissions_language FOREIGN KEY (language_id) REFERENCES languages (id) ON DELETE RESTRICT
);

-- Create submission_test_cases table
CREATE TABLE submission_test_cases (
    id BIGSERIAL PRIMARY KEY,
    submission_id BIGINT NOT NULL,
    test_case_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    execution_time_ms INTEGER,
    execution_memory_mb INTEGER,
    output TEXT,
    error_message TEXT,
    CONSTRAINT fk_stc_submission FOREIGN KEY (submission_id) REFERENCES submissions (id) ON DELETE CASCADE,
    CONSTRAINT fk_stc_testcase FOREIGN KEY (test_case_id) REFERENCES test_cases (id) ON DELETE CASCADE
);

