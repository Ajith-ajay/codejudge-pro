-- ============================================================
-- V8: Add problem listing support
-- ============================================================

-- Add fields required for problem listing
ALTER TABLE questions
    ADD COLUMN slug VARCHAR(255),
    ADD COLUMN published BOOLEAN NOT NULL DEFAULT FALSE;

-- Generate slugs for existing questions
UPDATE questions
SET slug = LOWER(
    REGEXP_REPLACE(
        REGEXP_REPLACE(TRIM(title), '[^a-zA-Z0-9]+', '-', 'g'),
        '(^-|-$)',
        '',
        'g'
    )
)
WHERE slug IS NULL;

-- Make slug mandatory after existing rows have been populated
ALTER TABLE questions
    ALTER COLUMN slug SET NOT NULL;


-- Tags
CREATE TABLE tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);


-- Question/tag relationship
CREATE TABLE question_tags (
    question_id BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (question_id, tag_id)
);


-- Submission indexes
CREATE INDEX idx_submission_question
    ON submissions(question_id);

CREATE INDEX idx_submission_user
    ON submissions(user_id);

CREATE INDEX idx_submission_status
    ON submissions(status);


-- Question listing indexes
CREATE UNIQUE INDEX idx_question_slug
    ON questions(slug);

CREATE INDEX idx_question_difficulty
    ON questions(difficulty);

CREATE INDEX idx_question_published
    ON questions(published);

CREATE INDEX IF NOT EXISTS idx_questions_type_difficulty
    ON questions(type, difficulty);

CREATE INDEX IF NOT EXISTS idx_questions_title_lower
    ON questions(LOWER(title));


-- Submission filtering indexes
CREATE INDEX IF NOT EXISTS idx_submissions_question_user_status
    ON submissions(question_id, user_id, status);

CREATE INDEX IF NOT EXISTS idx_submissions_practice_question_status
    ON submissions(question_id, status)
    WHERE candidate_id IS NULL;


-- Exam/section indexes
CREATE INDEX IF NOT EXISTS idx_section_questions_question
    ON section_questions(question_id);

CREATE INDEX IF NOT EXISTS idx_exam_end_time
    ON exams(end_time);