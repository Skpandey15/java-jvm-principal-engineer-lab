CREATE TABLE purchase_order (
    id           UUID PRIMARY KEY,
    customer_id  VARCHAR(64)    NOT NULL,
    total_amount NUMERIC(12, 2) NOT NULL,
    status       VARCHAR(16)    NOT NULL,
    created_at   TIMESTAMPTZ    NOT NULL,
    version      BIGINT         NOT NULL
);

CREATE INDEX idx_purchase_order_customer ON purchase_order (customer_id);
