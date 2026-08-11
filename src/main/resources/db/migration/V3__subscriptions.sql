-- V3__subscriptions.sql - create subscriptions and licenses

CREATE TABLE IF NOT EXISTS subscriptions (
  id BIGSERIAL PRIMARY KEY,
  external_id VARCHAR(100) UNIQUE,
  order_id BIGINT REFERENCES orders(id),
  contract_account VARCHAR(100),
  start_date timestamptz,
  end_date timestamptz,
  autorenew boolean DEFAULT true,
  status varchar(50),
  created_at timestamptz DEFAULT now()
);

CREATE TABLE IF NOT EXISTS licenses (
  id BIGSERIAL PRIMARY KEY,
  subscription_id BIGINT REFERENCES subscriptions(id),
  license_key text,
  status varchar(50),
  issued_at timestamptz,
  created_at timestamptz DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_subscriptions_order ON subscriptions(order_id);
