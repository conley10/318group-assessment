# AI-Assisted Travel Booking System (AI-Driven Development Case Study)

A microservices-based travel booking application built with **Spring Boot** and **Apache Kafka**. Four independent services — Catalogue, Booking, Payment and Notification — cooperate through asynchronous events to take a booking from creation, through inventory reservation and payment, to confirmation (or failure with compensation).

## Project Intention

This case study is designed to teach:

1. How to define requirements first (architecture, user stories, domain model, API contract, event contracts).
2. How implementation can be developed from those specs using AI-assisted workflows.
3. How to review and explain progress using a **before/after branch comparison**.

## Branches

Create and compare two branches: **spec-only** and **post-implementation**.

### Spec-Only Branch
Contains (1) the specification-first artifacts in the `specs/` folder
- `1-technical-architecture.md` — services, layers, stack, Kafka topics, design principles
- `2-user-stories.md` — user stories C1–C5, B1–B4, P1–P2, N1–N2
- `3-domain-models.md` — entities, enums, booking state machine, event contracts
- `4-api-endpoints.md` — REST endpoints per service and asynchronous event contracts
- `openapi-contract.yaml` — OpenAPI 3.0 schemas plus `x-kafka-events`

and (2) the prompt instructions in `agent_prompt.md` for the development agent.
(Auxiliary files such as the Maven wrapper are also included for convenience.)

### Post-Implementation Branch
Contains the full implementation with:
  - `catalogue-service` microservice
  - `booking-service` microservice
  - `payment-service` microservice
  - `notification-service` microservice

Each service is an independent Maven project with its own wrapper (there is no root aggregator POM yet).

## Project Structure

```text
318group-assessment/
│
├── catalogue-service/          # :8081 — packages, departures, inventory reservations
├── booking-service/            # :8082 — booking lifecycle coordinator
├── payment-service/            # :8083 — simulated payment gateway
├── notification-service/       # :8084 — booking-outcome notifications
│   │
│   │   (each service has the same layout)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/travelbooking/<service>/
│   │   │   │   ├── controller/     # REST API (@RestController)
│   │   │   │   ├── event/          # Kafka event records
│   │   │   │   ├── exception/      # global error handling (catalogue & booking only)
│   │   │   │   ├── messaging/      # Spring Cloud Stream consumers / producers
│   │   │   │   ├── model/          # JPA entities and enums
│   │   │   │   ├── repository/     # Spring Data JPA repositories
│   │   │   │   └── service/        # business logic
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   │       └── java/com/travelbooking/<service>/
│   ├── pom.xml
│   ├── mvnw
│   └── mvnw.cmd
│
├── specs/                      # specification-first artifacts
│   ├── 1-technical-architecture.md
│   ├── 2-user-stories.md
│   ├── 3-domain-models.md
│   ├── 4-api-endpoints.md
│   └── openapi-contract.yaml
│
├── docs/
│   └── setup-and-testing-guide.md   # detailed setup, manual testing, troubleshooting
│
├── agent_prompt.md             # instructions for the development agent
└── README.md                   # this file
```

## Agent-Assisted Development
To implement the application using a coding agent, follow these steps:
- check out the spec-only branch, and
- run your coding agent with the provided prompt instructions in `agent_prompt.md`.

## Running the Implemented Version

Prerequisites:

- Java 21
- Docker Desktop (to run Apache Kafka)
- Maven (or use the included Maven wrapper in each service)

Check out the post-implementation branch.

**1. Start Kafka** (first time creates the container; afterwards use `docker start travel-kafka`):

```powershell
docker run -d --name travel-kafka -p 9092:9092 apache/kafka:4.3.1
```

**2. Run the tests and start each service in its own terminal** (from the repository root):

```bash
cd catalogue-service    && ./mvnw clean test && ./mvnw spring-boot:run
cd booking-service      && ./mvnw clean test && ./mvnw spring-boot:run
cd payment-service      && ./mvnw clean test && ./mvnw spring-boot:run
cd notification-service && ./mvnw clean test && ./mvnw spring-boot:run
```

On Windows PowerShell, use:

```powershell
cd catalogue-service;    .\mvnw.cmd clean test; .\mvnw.cmd spring-boot:run
cd booking-service;      .\mvnw.cmd clean test; .\mvnw.cmd spring-boot:run
cd payment-service;      .\mvnw.cmd clean test; .\mvnw.cmd spring-boot:run
cd notification-service; .\mvnw.cmd clean test; .\mvnw.cmd spring-boot:run
```

| Component | Address |
| :--- | :--- |
| Apache Kafka | `localhost:9092` |
| Catalogue Service | `http://localhost:8081/api` |
| Booking Service | `http://localhost:8082/api` |
| Payment Service | `http://localhost:8083/api` |
| Notification Service | `http://localhost:8084/api` |

Each service exposes an H2 console at `/h2-console` (JDBC URL `jdbc:h2:mem:<service>db`, user `sa`, empty password).

**Interactive API docs (Swagger UI).** Every service includes [springdoc-openapi](https://springdoc.org), so once a service is running you can browse and call its endpoints from the browser — no Postman needed:

| Service | Swagger UI | Raw OpenAPI JSON |
| :--- | :--- | :--- |
| Catalogue | http://localhost:8081/swagger-ui.html | http://localhost:8081/v3/api-docs |
| Booking | http://localhost:8082/swagger-ui.html | http://localhost:8082/v3/api-docs |
| Payment | http://localhost:8083/swagger-ui.html | http://localhost:8083/v3/api-docs |
| Notification | http://localhost:8084/swagger-ui.html | http://localhost:8084/v3/api-docs |

In the UI, expand an endpoint → **Try it out** → edit the JSON body → **Execute**. The hand-written contract in `specs/openapi-contract.yaml` is the design-time spec; `/v3/api-docs` is what the code actually exposes.

For a step-by-step manual integration walkthrough (create package → departure → booking → observe events), Kafka console-producer commands, and troubleshooting, see **[docs/setup-and-testing-guide.md](docs/setup-and-testing-guide.md)**.

## Implemented Functionality

The implemented version is an event-driven Spring Boot microservice system for booking travel packages:

- **Catalogue Service** (`http://localhost:8081/api`)
  - Create, read, update, delete travel packages; search by destination
  - Add, read, delete package departures (dates, price, capacity)
  - Check departure availability (capacity minus active reservations)
  - Reserve, confirm and release inventory (via REST or Kafka events)
- **Booking Service** (`http://localhost:8082/api`)
  - Create, read, delete bookings
  - Booking lifecycle `PENDING_INVENTORY → PENDING_PAYMENT → CONFIRMED | FAILED` driven purely by events
  - Publishes `booking-created`, `payment-requested`, `booking-confirmed`, `booking-failed`
- **Payment Service** (`http://localhost:8083/api`)
  - Processes `payment-requested` events with a prototype gateway rule (`amount > 0` succeeds)
  - Publishes `payment-completed` / `payment-failed`; read payments by ID or booking
- **Notification Service** (`http://localhost:8084/api`)
  - Creates `BOOKING_CONFIRMED` / `BOOKING_FAILED` notifications from booking-outcome events
  - Read notifications by ID, customer or booking

Successful booking flow:

```text
Client ─POST /bookings─▶ Booking ─booking-created─▶ Catalogue ─inventory-reserved─▶ Booking
  ─payment-requested─▶ Payment ─payment-completed─▶ Booking (CONFIRMED)
  ─booking-confirmed─▶ Catalogue (reservation CONFIRMED) + Notification (BOOKING_CONFIRMED)
```

On payment failure the Booking becomes `FAILED` and publishes `booking-failed`, which releases the reservation in Catalogue (capacity restored) and creates a `BOOKING_FAILED` notification.
