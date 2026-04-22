-- V3__create_spoken_languages_table.sql
CREATE TABLE spoken_languages (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    language_code VARCHAR(10) NOT NULL,
    proficiency_level VARCHAR(50) NOT NULL CHECK (
        proficiency_level IN ('NATIVE', 'FLUENT', 'INTERMEDIATE', 'BEGINNER')
    ),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_spoken_languages_user_id ON spoken_languages(user_id);