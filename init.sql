-- ==============================================================================
-- Multilingual AI-Powered Agricultural Assistant — Full Database Schema
-- Compatible with Neon Serverless PostgreSQL and local PostgreSQL (pgvector)
-- ==============================================================================

CREATE EXTENSION IF NOT EXISTS vector;

-- 1. Authentication and User Management
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE,
    password_hash VARCHAR(255),
    full_name VARCHAR(255),
    phone VARCHAR(50),
    role VARCHAR(50) NOT NULL DEFAULT 'ROLE_FARMER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Ensure all required columns exist in case users table pre-existed
ALTER TABLE users ADD COLUMN IF NOT EXISTS email VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS full_name VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone VARCHAR(50);
ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(50) NOT NULL DEFAULT 'ROLE_FARMER';
ALTER TABLE users ADD COLUMN IF NOT EXISTS enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
UPDATE users SET email = 'farmer_' || id || '@kisan.local' WHERE email IS NULL;
UPDATE users SET password_hash = '$2a$12$e0MYzXy8Qz6m9jE8z6eKDeQ8p7g5zJ9W' WHERE password_hash IS NULL;

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Farmer Profile & Farm Memory
CREATE TABLE IF NOT EXISTS farmer_profiles (
    id SERIAL PRIMARY KEY,
    user_id INT UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    district VARCHAR(100),
    state VARCHAR(100) DEFAULT 'Tamil Nadu',
    preferred_language VARCHAR(10) DEFAULT 'en',
    primary_crop VARCHAR(100),
    total_land_acres NUMERIC(6,2),
    soil_type VARCHAR(100),
    irrigation_source VARCHAR(100),
    annual_budget_inr NUMERIC(10,2),
    consent_data_retention BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS farm_plots (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plot_name VARCHAR(100) NOT NULL,
    area_acres NUMERIC(6,2) NOT NULL,
    current_crop VARCHAR(100),
    crop_stage VARCHAR(100),
    location VARCHAR(255),
    soil_test_ph NUMERIC(4,2),
    soil_organic_carbon NUMERIC(4,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS farm_reminders (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    due_date DATE NOT NULL,
    completed BOOLEAN DEFAULT FALSE,
    related_crop VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS crop_plans (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    crop_name VARCHAR(100) NOT NULL,
    season VARCHAR(50),
    land_area_acres NUMERIC(6,2),
    total_budget_inr NUMERIC(10,2),
    estimated_cost_inr NUMERIC(10,2),
    expected_savings_inr NUMERIC(10,2),
    plan_type VARCHAR(50) NOT NULL,
    cost_breakdown_json TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS scheme_assessments (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    scheme_code VARCHAR(100) NOT NULL,
    scheme_name VARCHAR(255) NOT NULL,
    is_eligible BOOLEAN NOT NULL,
    satisfied_criteria_json TEXT,
    missing_criteria_json TEXT,
    official_url VARCHAR(1024),
    assessed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Queries, Responses, and Sources (Farmer Interactions)
CREATE TABLE IF NOT EXISTS queries (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE SET NULL,
    question TEXT NOT NULL,
    language VARCHAR(10) NOT NULL,
    location VARCHAR(255),
    season VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS responses (
    id SERIAL PRIMARY KEY,
    query_id INT REFERENCES queries(id) ON DELETE CASCADE,
    answer TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sources (
    id SERIAL PRIMARY KEY,
    response_id INT REFERENCES responses(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    url VARCHAR(1024)
);

-- 4. RAG Knowledge Base Documents and Vector Chunks
CREATE TABLE IF NOT EXISTS documents (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    source_organization VARCHAR(255) NOT NULL,
    url VARCHAR(1024),
    retrieved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS document_chunks (
    id SERIAL PRIMARY KEY,
    document_id INT REFERENCES documents(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    chunk_index INT NOT NULL,
    embedding VECTOR
);

-- Indexes for Fast Lookups
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_queries_user_id ON queries(user_id);
CREATE INDEX IF NOT EXISTS idx_queries_created_at ON queries(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_responses_query_id ON responses(query_id);
CREATE INDEX IF NOT EXISTS idx_sources_response_id ON sources(response_id);
CREATE INDEX IF NOT EXISTS idx_farmer_profiles_user ON farmer_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_farm_plots_user ON farm_plots(user_id);
CREATE INDEX IF NOT EXISTS idx_farm_reminders_user ON farm_reminders(user_id, due_date);
CREATE INDEX IF NOT EXISTS idx_crop_plans_user ON crop_plans(user_id);
CREATE INDEX IF NOT EXISTS idx_scheme_assessments_user ON scheme_assessments(user_id);
