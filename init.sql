CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE queries (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id),
    question TEXT NOT NULL,
    language VARCHAR(10) NOT NULL,
    location VARCHAR(255),
    season VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE responses (
    id SERIAL PRIMARY KEY,
    query_id INT REFERENCES queries(id),
    answer TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sources (
    id SERIAL PRIMARY KEY,
    response_id INT REFERENCES responses(id),
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    url VARCHAR(1024)
);

-- Note: The vector/chunk table schema will be finalized in Phase 2
-- once the embedding model and dimension size are selected.
-- Example for future reference:
-- CREATE TABLE document_chunks (
--     id SERIAL PRIMARY KEY,
--     content TEXT NOT NULL,
--     embedding VECTOR(768) -- Dimension size depends on the model
-- );
