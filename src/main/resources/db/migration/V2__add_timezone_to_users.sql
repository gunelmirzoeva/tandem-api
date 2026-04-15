-- V2__add_timezone_to_users.sql

ALTER TABLE users
ADD COLUMN IF NOT EXISTS timezone VARCHAR(100) NOT NULL DEFAULT 'UTC';