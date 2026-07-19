-- Create questions table
CREATE TABLE questions (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    marks INTEGER NOT NULL,
    type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Create mcq_questions table (inherits from questions)
CREATE TABLE mcq_questions (
    id BIGINT PRIMARY KEY,
    options JSONB NOT NULL,
    is_multiple_choice BOOLEAN DEFAULT FALSE NOT NULL,
    negative_marking NUMERIC(5, 2) DEFAULT 0.00 NOT NULL,
    partial_marking BOOLEAN DEFAULT FALSE NOT NULL,
    randomize_options BOOLEAN DEFAULT FALSE NOT NULL,
    CONSTRAINT fk_mcq_questions_question FOREIGN KEY (id) REFERENCES questions (id) ON DELETE CASCADE
);

-- Create coding_questions table (inherits from questions)
CREATE TABLE coding_questions (
    id BIGINT PRIMARY KEY,
    constraints TEXT,
    time_limit_ms INTEGER DEFAULT 1000 NOT NULL,
    memory_limit_mb INTEGER DEFAULT 256 NOT NULL,
    CONSTRAINT fk_coding_questions_question FOREIGN KEY (id) REFERENCES questions (id) ON DELETE CASCADE
);

-- Create languages table
CREATE TABLE languages (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    code VARCHAR(20) UNIQUE NOT NULL,
    compiler_version VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Create coding_question_languages join table
CREATE TABLE coding_question_languages (
    coding_question_id BIGINT NOT NULL,
    language_id BIGINT NOT NULL,
    PRIMARY KEY (coding_question_id, language_id),
    CONSTRAINT fk_cql_question FOREIGN KEY (coding_question_id) REFERENCES coding_questions (id) ON DELETE CASCADE,
    CONSTRAINT fk_cql_language FOREIGN KEY (language_id) REFERENCES languages (id) ON DELETE CASCADE
);

-- Create test_cases table
CREATE TABLE test_cases (
    id BIGSERIAL PRIMARY KEY,
    coding_question_id BIGINT NOT NULL,
    input TEXT NOT NULL,
    expected_output TEXT NOT NULL,
    is_hidden BOOLEAN DEFAULT FALSE NOT NULL,
    marks INTEGER DEFAULT 0 NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_test_cases_question FOREIGN KEY (coding_question_id) REFERENCES coding_questions (id) ON DELETE CASCADE
);

-- Create section_questions join table
CREATE TABLE section_questions (
    section_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    order_index INTEGER NOT NULL,
    PRIMARY KEY (section_id, question_id),
    CONSTRAINT fk_sq_section FOREIGN KEY (section_id) REFERENCES sections (id) ON DELETE CASCADE,
    CONSTRAINT fk_sq_question FOREIGN KEY (question_id) REFERENCES questions (id) ON DELETE CASCADE
);

-- Seed basic programming languages
INSERT INTO languages (name, code, compiler_version) VALUES
('Java 21', 'java', 'openjdk 21'),
('Python 3', 'python', 'python 3.11'),
('C++', 'cpp', 'g++ 12.2'),
('C', 'c', 'gcc 12.2'),
('JavaScript (Node.js)', 'javascript', 'node 18.16');
