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

-- Knowledge Base Tables
CREATE TABLE documents (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    source_organization VARCHAR(255) NOT NULL,
    url VARCHAR(1024),
    retrieved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Note: The vector dimension size is omitted here as it will be 
-- finalized once the embedding model is selected.
CREATE TABLE document_chunks (
    id SERIAL PRIMARY KEY,
    document_id INT REFERENCES documents(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    chunk_index INT NOT NULL,
    embedding VECTOR
);
