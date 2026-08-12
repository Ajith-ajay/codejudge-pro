ALTER TABLE submissions
    ADD COLUMN IF NOT EXISTS assessment_id BIGINT;

ALTER TABLE submissions
    ADD CONSTRAINT fk_submissions_assessment
    FOREIGN KEY (assessment_id)
    REFERENCES learning_assessments(id)
    ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_submissions_assessment
    ON submissions(assessment_id);

CREATE INDEX IF NOT EXISTS idx_submissions_user_assessment
    ON submissions(user_id, assessment_id);
