-- Add user_id to submissions table and make candidate_id nullable
ALTER TABLE submissions ADD COLUMN user_id BIGINT;
ALTER TABLE submissions ALTER COLUMN candidate_id DROP NOT NULL;

-- Backfill user_id from exam_candidates for existing submissions (standard SQL compatible with H2 & Postgres)
UPDATE submissions
SET user_id = (
    SELECT user_id 
    FROM exam_candidates 
    WHERE exam_candidates.id = submissions.candidate_id
)
WHERE candidate_id IS NOT NULL;

-- Seed default user_id (e.g. candidate user id 2) for any orphaned submissions where candidate is not found or null
UPDATE submissions
SET user_id = 2
WHERE user_id IS NULL;

-- Enforce NOT NULL constraint on user_id
ALTER TABLE submissions ALTER COLUMN user_id SET NOT NULL;

-- Add foreign key constraint
ALTER TABLE submissions ADD CONSTRAINT fk_submissions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
