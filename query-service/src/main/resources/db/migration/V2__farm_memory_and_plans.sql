-- V2: Farm Memory, Profiles, Plots, Reminders, Crop Plans, and Security Tokens

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

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

CREATE INDEX IF NOT EXISTS idx_reset_token_hash ON password_reset_tokens(token_hash);
CREATE INDEX IF NOT EXISTS idx_farmer_profiles_user ON farmer_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_farm_plots_user ON farm_plots(user_id);
CREATE INDEX IF NOT EXISTS idx_farm_reminders_user ON farm_reminders(user_id, due_date);
CREATE INDEX IF NOT EXISTS idx_crop_plans_user ON crop_plans(user_id);
CREATE INDEX IF NOT EXISTS idx_scheme_assessments_user ON scheme_assessments(user_id);
