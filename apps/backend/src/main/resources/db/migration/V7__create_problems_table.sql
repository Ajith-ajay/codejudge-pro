CREATE TABLE problems (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    tags VARCHAR(255),
    difficulty VARCHAR(50),
    acceptance_rate DOUBLE PRECISION,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
