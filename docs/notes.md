# Note: This file intentionally minimal - expand as you implement more business logic

The important classes to look into:
- OrderService.createAndProcessOrder -> core path: persist order, create subscription, publish BRIM request
- LicenseService.pollForLicenseKeys -> scheduled poll (runs every 4 hours) to create license requests
- KafkaProducerConfig / Kafka topics: brim-contract-requests, brim-contract-events

Integration points (where to implement concrete calls):
- IntegrationService (not included fully): call BRIM via REST or publish to Kafka
- PDAPI/QLS adapter: call external licensing service to request licenses

Database: Postgres with Flyway V1 migration already created.
