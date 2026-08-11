%% Data model (ER) - simplified

erDiagram
    CUSTOMERS {
        BIGINT id PK
        VARCHAR external_id
        VARCHAR name
        VARCHAR email
    }
    ORDERS {
        BIGINT id PK
        VARCHAR external_id
        BIGINT customer_id FK
        VARCHAR product_code
        NUMERIC amount
        VARCHAR currency
        VARCHAR status
    }
    SUBSCRIPTIONS {
        BIGINT id PK
        VARCHAR external_id
        BIGINT order_id FK
        VARCHAR contract_account
        TIMESTAMP start_date
        TIMESTAMP end_date
        BOOLEAN autorenew
        VARCHAR status
    }
    LICENSES {
        BIGINT id PK
        BIGINT subscription_id FK
        TEXT license_key
        VARCHAR status
        TIMESTAMP issued_at
    }
    OUTBOX_EVENTS {
        BIGINT id PK
        VARCHAR aggregate_type
        VARCHAR aggregate_id
        VARCHAR type
        JSONB payload
        BOOLEAN published
    }

    CUSTOMERS ||--o{ ORDERS : has
    ORDERS ||--o{ SUBSCRIPTIONS : creates
    SUBSCRIPTIONS ||--o{ LICENSES : issues
    ORDERS ||--o{ OUTBOX_EVENTS : emits
    SUBSCRIPTIONS ||--o{ OUTBOX_EVENTS : emits
