# Flyway migration - schema for the core entities

-- V1__init.sql
CREATE TABLE customers (
    id bigserial PRIMARY KEY,
    external_id varchar(100) UNIQUE,
    name varchar(255) NOT NULL,
    email varchar(255) NOT NULL,
    created_at timestamptz DEFAULT now()
);

CREATE TABLE orders (
    id bigserial PRIMARY KEY,
    external_id varchar(100) UNIQUE,
    customer_id bigint REFERENCES customers(id),
    product_code varchar(100),
    amount numeric(12,2),
    currency varchar(10),
    status varchar(50),
    created_at timestamptz DEFAULT now()
);

CREATE TABLE subscriptions (
    id bigserial PRIMARY KEY,
    external_id varchar(100) UNIQUE,
    order_id bigint REFERENCES orders(id),
    contract_account varchar(100),
    start_date timestamptz,
    end_date timestamptz,
    autorenew boolean DEFAULT true,
    status varchar(50),
    created_at timestamptz DEFAULT now()
);

CREATE TABLE licenses (
    id bigserial PRIMARY KEY,
    subscription_id bigint REFERENCES subscriptions(id),
    license_key text,
    status varchar(50),
    created_at timestamptz DEFAULT now()
);

CREATE INDEX idx_orders_customer ON orders(customer_id);
CREATE INDEX idx_subscriptions_order ON subscriptions(order_id);
