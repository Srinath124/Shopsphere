-- V1: Initial schema for product_db

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS categories (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_categories_name ON categories(name);

CREATE TABLE IF NOT EXISTS products (
    id          UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(200) NOT NULL,
    description TEXT,
    price       NUMERIC(12, 2) NOT NULL CHECK (price > 0),
    category_id UUID         NOT NULL REFERENCES categories(id),
    image_url   VARCHAR(500),
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_products_category_id ON products(category_id);
CREATE INDEX IF NOT EXISTS idx_products_active       ON products(active);
CREATE INDEX IF NOT EXISTS idx_products_name         ON products(name);

-- Seed categories
INSERT INTO categories (id, name, description) VALUES
    (uuid_generate_v4(), 'Electronics',   'Electronic devices and accessories'),
    (uuid_generate_v4(), 'Clothing',      'Apparel and fashion'),
    (uuid_generate_v4(), 'Books',         'Physical and digital books'),
    (uuid_generate_v4(), 'Home & Garden', 'Home improvement and gardening'),
    (uuid_generate_v4(), 'Sports',        'Sports equipment and accessories')
ON CONFLICT (name) DO NOTHING;

-- Seed sample products
INSERT INTO products (id, name, description, price, category_id, image_url, active)
SELECT
    uuid_generate_v4(),
    'Wireless Headphones Pro',
    'Premium noise-cancelling wireless headphones with 30-hour battery life',
    149.99,
    c.id,
    'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400',
    true
FROM categories c WHERE c.name = 'Electronics'
ON CONFLICT DO NOTHING;

INSERT INTO products (id, name, description, price, category_id, image_url, active)
SELECT
    uuid_generate_v4(),
    'Mechanical Keyboard RGB',
    'Full-size mechanical gaming keyboard with customizable RGB lighting',
    89.99,
    c.id,
    'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=400',
    true
FROM categories c WHERE c.name = 'Electronics'
ON CONFLICT DO NOTHING;

INSERT INTO products (id, name, description, price, category_id, image_url, active)
SELECT
    uuid_generate_v4(),
    'Running Shoes Elite',
    'Lightweight performance running shoes with carbon fiber plate',
    199.99,
    c.id,
    'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400',
    true
FROM categories c WHERE c.name = 'Sports'
ON CONFLICT DO NOTHING;
