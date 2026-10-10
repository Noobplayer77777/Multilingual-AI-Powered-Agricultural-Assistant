# Setup Instructions

## Prerequisites
- Java 21
- Maven
- Docker and Docker Compose

## Starting the Database
1. Copy `.env.example` to `.env` and fill in the values (leave defaults for local dev).
2. Run `docker-compose up -d` to start the PostgreSQL instance with `pgvector` enabled.

## Running the Services
The services are standard Spring Boot applications.
From the root directory:
```bash
# Query Service (Port 8081)
cd query-service
mvn spring-boot:run

# Advisory Service (Port 8082)
cd ../advisory-service
mvn spring-boot:run
```
