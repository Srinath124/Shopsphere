-- V1: Initial schema for order_db

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS orders (
    id               UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id          VARCHAR(255) NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    total_amount     NUMERIC(12, 2) NOT NULL,
    idempotency_key  VARCHAR(100) UNIQUE,
    notes            VARCHAR(500),
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_orders_user_id         ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status          ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_created_at      ON orders(created_at DESC);
CREATE UNIQUE INDEX IF NOT EXISTS idx_orders_idempotency ON orders(idempotency_key) WHERE idempotency_key IS NOT NULL;

CREATE TABLE IF NOT EXISTS order_items (
    id           UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id     UUID         NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id   UUID         NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    unit_price   NUMERIC(12, 2) NOT NULL,
    quantity     INTEGER      NOT NULL CHECK (quantity > 0)
);

CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);
