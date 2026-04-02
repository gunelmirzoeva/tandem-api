-- V1__create_users_table.sql
CREATE TABLE users (
   id UUID PRIMARY KEY,
   full_name VARCHAR(255) NOT NULL,
   email VARCHAR(255) UNIQUE NOT NULL,
   password_hash VARCHAR(255) NOT NULL,
   role VARCHAR(50) NOT NULL,
   status VARCHAR(50) NOT NULL,
   created_at TIMESTAMP NOT NULL DEFAULT now()
);
