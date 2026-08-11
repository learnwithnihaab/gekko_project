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
