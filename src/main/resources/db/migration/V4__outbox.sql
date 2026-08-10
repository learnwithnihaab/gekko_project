-- V4__outbox.sql - simple outbox table

CREATE TABLE IF NOT EXISTS outbox_events (
  id BIGSERIAL PRIMARY KEY,
  aggregate_type varchar(100),
  aggregate_id varchar(100),
  type varchar(100),
  payload jsonb,
  published boolean DEFAULT false,
  created_at timestamptz DEFAULT now()
);
