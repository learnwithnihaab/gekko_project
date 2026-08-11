-- V2__orders.sql - create orders table

CREATE TABLE IF NOT EXISTS orders (
  id BIGSERIAL PRIMARY KEY,
  external_id VARCHAR(100) UNIQUE,
  customer_id BIGINT REFERENCES customers(id),
  product_code VARCHAR(100),
  amount numeric(12,2),
  currency VARCHAR(10),
  status VARCHAR(50),
  created_at timestamptz DEFAULT now(),
  updated_at timestamptz
);

CREATE INDEX IF NOT EXISTS idx_orders_customer ON orders(customer_id);
