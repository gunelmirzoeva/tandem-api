-- V7__create_availability_blocks_table.sql

CREATE TABLE availability_blocks (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    day_of_week VARCHAR(50) NOT NULL CHECK (
        day_of_week IN ('MONDAY', 'TUESDAY', 'WEDNESDAY',
                       'THURSDAY', 'FRIDAY', 'SATURDAY',
                       'SUNDAY')
        ),
    start_time_utc TIME NOT NULL,
    end_time_utc TIME NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_availability_blocks_user_day ON availability_blocks(user_id, day_of_week);