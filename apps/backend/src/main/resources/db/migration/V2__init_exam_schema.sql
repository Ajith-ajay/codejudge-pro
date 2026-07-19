-- Create exams table
CREATE TABLE exams (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    duration_minutes INTEGER NOT NULL,
    pass_percentage NUMERIC(5, 2) DEFAULT 0.00 NOT NULL,
    is_published BOOLEAN DEFAULT FALSE NOT NULL,
    is_closed BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Create sections table
CREATE TABLE sections (
    id BIGSERIAL PRIMARY KEY,
    exam_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    order_index INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_sections_exam FOREIGN KEY (exam_id) REFERENCES exams (id) ON DELETE CASCADE
);

-- Create exam_candidates table
CREATE TABLE exam_candidates (
    id BIGSERIAL PRIMARY KEY,
    exam_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    invited_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    joined_at TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    score NUMERIC(5, 2),
    passed BOOLEAN,
    status VARCHAR(30) DEFAULT 'INVITED' NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_exam_candidates_exam FOREIGN KEY (exam_id) REFERENCES exams (id) ON DELETE CASCADE,
    CONSTRAINT fk_exam_candidates_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_exam_candidate UNIQUE (exam_id, user_id)
);
