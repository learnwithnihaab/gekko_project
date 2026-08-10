# Gekko Project - Complete Implementation

This repository contains a complete Spring Boot backend implementation for the Gekko subscription management platform.

High level:
- Java 11 + Spring Boot 2.7
- Postgres for persistence (Flyway migrations provided)
- Kafka for asynchronous integrations with BRIM / PDAPI
- Redis for caching
- JWT-based security (Client Credentials style stub)
- Docker Compose for local dev: Postgres, Redis, Kafka, Zookeeper

Branch: feature/complete-project

Run locally:
- mvn clean package
- docker-compose up -d
- Configure application.yml if needed and run: java -jar target/gekko-service-0.0.1-SNAPSHOT.jar

What is included:
- Entities: Customer, Order, Subscription, ContractAccount, License, Payment, NotificationEvent
- Repositories: Spring Data JPA repos
- Services: OrderService (commerce controller), SubscriptionService, LicenseService, IntegrationService
- Controllers: OrderController, SubscriptionController, AuthController
- Kafka producers/consumers to simulate Gekko <-> BRIM interactions
- Flyway migration scripts to create DB schema
- application.yml with dev config

Each .java has detailed comments explaining purpose and flow.
