-- Create activity_logs table
CREATE TABLE activity_logs (
    id BIGSERIAL PRIMARY KEY,
    exam_candidate_id BIGINT NOT NULL,
    activity_type VARCHAR(50) NOT NULL,
    details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_activity_logs_candidate FOREIGN KEY (exam_candidate_id) REFERENCES exam_candidates (id) ON DELETE CASCADE
);

-- Create notifications table
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Create certificates table
CREATE TABLE certificates (
    id BIGSERIAL PRIMARY KEY,
    exam_candidate_id BIGINT UNIQUE NOT NULL,
    certificate_code VARCHAR(100) UNIQUE NOT NULL,
    issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_certificates_candidate FOREIGN KEY (exam_candidate_id) REFERENCES exam_candidates (id) ON DELETE CASCADE
);
