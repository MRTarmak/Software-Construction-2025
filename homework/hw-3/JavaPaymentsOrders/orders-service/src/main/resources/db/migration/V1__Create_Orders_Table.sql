CREATE TYPE order_status AS ENUM ('NEW', 'FINISHED', 'CANCELLED');

CREATE TABLE IF NOT EXISTS order_table (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    description TEXT,
    status order_status NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);