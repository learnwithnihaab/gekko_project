# Gekko Project

Gekko Subscription Management Project.

## Current Implementation

- Java 11
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- Kafka
- Redis
- Docker Compose

## Run locally

```bash
docker compose up -d
mvn clean package
mvn spring-boot:run
```

## Health Endpoint

GET /api/v1/health


## client
You can onboard upstream systems (Storefront, Argon) into Gekko by creating a client record (apiKey + secret) and use X-Client-Id / X-Client-Secret headers for requests when JWT is not used or when APIGEE does not forward a JWT.
The ClientAuthFilter accepts those headers and sets a ROLE_CLIENT principal so code protected by Spring Security will accept the call.
Admin endpoints to create clients are intended for admin use only — in production restrict them to admin roles and store secrets securely (hash them / use a secrets manager).

How to verify locally

Update local branch and build:
git fetch origin
git checkout feature/complete-project-3
git pull origin feature/complete-project-3
mvn test (runs unit tests)
mvn -DskipTests package
Run app and create a client (admin):
java -jar target/gekko-service-0.0.1-SNAPSHOT.jar
POST (admin) to create a client: curl -X POST "http://localhost:8080/internal/admin/clients/create?name=HP-Store" — response includes apiKey and apiSecret (show once)
Call protected endpoints with client headers (if you don’t want to use JWT):
Example: curl -X POST -H "X-Client-Id: <apiKey>" -H "X-Client-Secret: <apiSecret>"
-H "Content-Type: application/json" -d @order.json
http://localhost:8080/api/v1/orders
Notes / security caveats

The admin create-client endpoint currently returns the secret in plain text once; in production you must store secrets hashed (bcrypt) and only display plain secret once at creation, or use a secure secret store.
Prefer APIGEE to handle onboarding + client credentials and to forward a validated JWT; if APIGEE does that you can ignore the client-header flow or use it for internal testing.
Admin endpoints should be restricted to ROLE_ADMIN in production (I can add that next).

## things till now:
Items that are partially implemented or are stubs (need production completion)
PDAPI/QLS: the client is a resilient stub; you must provide the actual PDAPI base URL, request/response contract and authentication (API key / OAuth / mTLS) so I can finish mapping and parsing responses and integrate real email hooks.
BRIM outbound (calling BRIM from Gekko when order is created): currently the system receives BRIM callbacks; the outbound synchronous call from OrderService to BRIM (if required) is not yet implemented.
APIGEE onboarding flow: we have a Client entity and an API-key filter in Gekko, but a production deployment typically uses APIGEE to manage onboarding and authentication (APIGEE → Gekko JWT forwarding). If you want Gekko itself to manage client secrets, we should:
Hash secrets when stored (bcrypt).
Add admin UX to rotate/revoke keys.
Add secure distribution (do not show secrets in plain text after creation).
GTR / ETR / SAP SV flows: not implemented (these are external orchestration steps such as hold-for-20-min, ETR verification, SAP SV interactions). You have webhook processing for BRIM but no direct modeling of GTR/ETR flows.
Mail-service: not integrated — PDAPI may send email in your environment; if you want Gekko to send mail, we need to add a MailService integration (SMTP or SES).
Data-parser for Argon: not implemented.
Order cancellation / auto-renew ON/OFF endpoints: not implemented (can be added; DB has subscription fields).
Redis: present in dependencies and docker-compose but not yet used via CacheManager or @Cacheable annotations on read-heavy endpoints.
Full admin UI (production-grade): only a minimal prototype exists.
Observability: Prometheus/Micrometer added; tracing (OpenTelemetry) and detailed dashboards are not added yet.
Idempotency/correlation across all external calls: basic safeguards present (external IDs, outbox), but production-grade idempotency keys and dedupe for all BRIM/PDAPI calls should be added.
AWS Kinesis integration (you mentioned APIGEE → AWS API Gateway → AWS Kinesis): not implemented.
Exact file locations for the primary pieces (so you can inspect quickly)
Entities: src/main/java/com/gekko/entity/*.java
Repositories: src/main/java/com/gekko/repository/*.java
Order API & DTO: src/main/java/com/gekko/controller/OrderController.java and src/main/java/com/gekko/dto/OrderRequest.java
Outbox & publisher: src/main/java/com/gekko/outbox/*
Kafka config & producers/listeners: src/main/java/com/gekko/config/KafkaConfig.java, /messaging/, /listener/
BRIM webhook: src/main/java/com/gekko/webhook/BrimWebhookController.java
PDAPI client: src/main/java/com/gekko/integration/PdapiClient.java
LicenseService: src/main/java/com/gekko/service/impl/LicenseServiceImpl.java
Client onboarding + filter: src/main/java/com/gekko/entity/Client.java, /service/impl/ClientServiceImpl.java, /controller/ClientController.java, /security/ClientAuthFilter.java
Security config: src/main/java/com/gekko/security/SecurityConfig.java
Admin UI prototype: docs/admin-ui/index.html
OpenAPI and diagrams: docs/openapi.yaml, docs/diagrams/*
Flyway migrations: src/main/resources/db/migration/V1__.. to V4__..
Tests: src/test/java/com/gekko/...
How you can verify locally (exact commands)
git checkout feature/complete-project-3
docker compose up -d
mvn test
mvn -DskipTests package
java -jar target/gekko-service-0.0.1-SNAPSHOT.jar
Use the dev JWT (HMAC secret in app.jwt.secret) or create a client via the admin endpoint and call the orders endpoint using headers X-Client-Id / X-Client-Secret.
Simulate BRIM webhook by POSTing the example payload to /internal/webhooks/brim and inspect DB tables (orders, subscriptions, licenses, outbox_events).
Consume Kafka topics (gekko.orders.created, gekko.subscriptions.created) to verify events flow.
