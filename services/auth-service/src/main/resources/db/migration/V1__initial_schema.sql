-- V1: Initial schema for auth_db
-- Creates users table with UUID PK, role-based authorization

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS users (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username    VARCHAR(50)  NOT NULL UNIQUE,
    email       VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role        VARCHAR(20)  NOT NULL DEFAULT 'USER',
    enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email    ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role     ON users(role);

-- Seed an admin user (password: Admin@1234 BCrypt encoded)
INSERT INTO users (id, username, email, password_hash, role, enabled)
VALUES (
    uuid_generate_v4(),
    'admin',
    'admin@shopsphere.com',
    '$2a$12$9jGEV2PU2qk.SDu75ILk5OEgPnYrPPF2cKI2oqd27z/EzpVGh64uK',
    'ADMIN',
    TRUE
) ON CONFLICT (username) DO NOTHING;
