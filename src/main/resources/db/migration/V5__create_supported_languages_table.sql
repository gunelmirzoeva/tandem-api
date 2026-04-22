-- V5__create_supported_languages_table.sql

CREATE TABLE supported_languages (
    language_code VARCHAR(10) PRIMARY KEY,
    language_name VARCHAR(100) NOT NULL UNIQUE
);