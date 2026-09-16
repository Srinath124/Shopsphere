-- V1: Initial schema for inventory_db

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS inventory (
    id                UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id        UUID         NOT NULL UNIQUE,
    total_quantity    INTEGER      NOT NULL DEFAULT 0 CHECK (total_quantity >= 0),
    reserved_quantity INTEGER      NOT NULL DEFAULT 0 CHECK (reserved_quantity >= 0),
    version           BIGINT       NOT NULL DEFAULT 0,
    updated_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_reserved_lte_total CHECK (reserved_quantity <= total_quantity)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_inventory_product_id ON inventory(product_id);
