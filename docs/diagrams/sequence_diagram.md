%% Sequence diagram: Gekko core flow (external webhook + internal kafka)

sequenceDiagram
    participant Storefront
    participant APIGEE
    participant Gekko_API
    participant DB
    participant OutboxPublisher
    participant Kafka
    participant BRIM
    participant Gekko_Webhook
    participant SubscriptionListener
    participant PDAPI

    Storefront->>APIGEE: POST /order (client credentials)
    APIGEE->>Gekko_API: Forward request (authenticated)
    Gekko_API->>DB: Insert order row + insert outbox event (OrderCreated)
    DB-->>Gekko_API: commit
    OutboxPublisher->>DB: poll outbox_events
    OutboxPublisher->>Kafka: publish gekko.orders.created
    Kafka->>SubscriptionListener: deliver event (internal service)
    BRIM-->>Gekko_Webhook: POST /internal/webhooks/brim (contract created)
    Gekko_Webhook->>DB: create/update subscription + outbox event SubscriptionCreated
    OutboxPublisher->>Kafka: publish gekko.subscriptions.created
    Kafka->>SubscriptionListener: deliver subscription.created
    SubscriptionListener->>PDAPI: requestLicense(contractAccount)
    PDAPI-->>SubscriptionListener: licenseKey
    SubscriptionListener->>DB: persist license
