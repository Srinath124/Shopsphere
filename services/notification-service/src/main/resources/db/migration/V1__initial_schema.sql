-- V1: Initial schema for notification_db

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS notifications (
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id    VARCHAR(255) NOT NULL,
    order_id   UUID,
    event_type VARCHAR(50)  NOT NULL,
    channel    VARCHAR(20)  NOT NULL DEFAULT 'EMAIL',
    recipient  VARCHAR(200),
    subject    VARCHAR(200),
    body       TEXT,
    status     VARCHAR(20)  NOT NULL DEFAULT 'SENT',
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notifications_user_id    ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_order_id   ON notifications(order_id);
CREATE INDEX IF NOT EXISTS idx_notifications_event_type ON notifications(event_type);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications(created_at DESC);
