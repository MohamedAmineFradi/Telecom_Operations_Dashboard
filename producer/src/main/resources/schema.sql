CREATE TABLE IF NOT EXISTS watermarks (
    stream_name VARCHAR(64) PRIMARY KEY,
    last_processed TIMESTAMPTZ NOT NULL
);