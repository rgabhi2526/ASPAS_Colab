USE aspas_db;

ALTER TABLE order_items
ADD COLUMN fulfilled BOOLEAN NOT NULL DEFAULT FALSE AFTER required_qty;

CREATE INDEX idx_order_items_fulfilled ON order_items (fulfilled);
