-- V4__create_target_languages_table.sql

CREATE TABLE target_languages (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    language_code VARCHAR(10) NOT NULL,
    current_level VARCHAR(50) NOT NULL CHECK (
        current_level IN ('FLUENT', 'INTERMEDIATE', 'BEGINNER')
    ),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_target_languages_user_id ON target_languages(user_id);