-- Create permissions table
CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Create roles table
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Create role_permissions join table
CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions (id) ON DELETE CASCADE
);

-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    enabled BOOLEAN DEFAULT FALSE NOT NULL,
    email_verified BOOLEAN DEFAULT FALSE NOT NULL,
    verification_token VARCHAR(255),
    reset_token VARCHAR(255),
    reset_token_expiry TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Create user_roles join table
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

-- Create refresh_tokens table
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    revoked BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Create audit_logs table
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    action VARCHAR(255) NOT NULL,
    ip_address VARCHAR(45),
    user_agent VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Seed basic roles
INSERT INTO roles (name, description) VALUES
('ROLE_SUPER_ADMIN', 'Super administrator with full access to the system'),
('ROLE_ADMIN', 'Administrator to manage system parameters, exams, users, and logs'),
('ROLE_EXAM_SETTER', 'Authorized to create, edit and schedule coding and MCQ exams'),
('ROLE_CANDIDATE', 'Students or candidates taking exams');

-- Seed basic permissions
INSERT INTO permissions (name, description) VALUES
('MANAGE_SYSTEM', 'Access all admin control panels'),
('CREATE_EXAM', 'Create exams and assessments'),
('EDIT_EXAM', 'Modify exams and assessment configs'),
('DELETE_EXAM', 'Remove exams and assessments'),
('PUBLISH_EXAM', 'Publish and open scheduled exams'),
('VIEW_REPORTS', 'Generate and view assessment logs and candidate reports'),
('TAKE_EXAM', 'Participate and submit assessments'),
('VIEW_LEADERBOARD', 'View real-time candidate scores and percentiles');

-- Link roles and permissions
-- Super Admin gets everything (IDs 1-8)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'ROLE_SUPER_ADMIN';

-- Admin gets everything except MANAGE_SYSTEM in some strict scopes, but let's give them all standard exam + report permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'ROLE_ADMIN' AND p.name IN ('CREATE_EXAM', 'EDIT_EXAM', 'DELETE_EXAM', 'PUBLISH_EXAM', 'VIEW_REPORTS', 'VIEW_LEADERBOARD');

-- Exam Setter gets create, edit, delete, publish exam, and view leaderboard
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'ROLE_EXAM_SETTER' AND p.name IN ('CREATE_EXAM', 'EDIT_EXAM', 'DELETE_EXAM', 'PUBLISH_EXAM', 'VIEW_LEADERBOARD');

-- Candidate gets only TAKE_EXAM and VIEW_LEADERBOARD
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'ROLE_CANDIDATE' AND p.name IN ('TAKE_EXAM', 'VIEW_LEADERBOARD');
