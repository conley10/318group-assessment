# AI-Assisted Travel Booking System (AI-Driven Development Case Study)

A microservices-based travel booking application built with **Spring Boot**, **Apache Kafka**, **Kafka Streams**, and **LangChain4j**.

Six independent services — Catalogue, Booking, Payment, Notification, Travel Assistant and Stream Analytics — work together to provide an event-driven travel booking platform with AI-assisted recommendations and real-time booking analytics.

The core booking workflow takes a booking from creation through inventory reservation and payment to confirmation, or handles failure through compensating events. The Travel Assistant provides AI-powered recommendations using live Catalogue data, while Stream Analytics processes booking events in real time.

---

## Project Intention

This case study is designed to demonstrate:

1. How to define requirements first through architecture, user stories, domain models, API contracts and event contracts.
2. How a microservice implementation can be developed from those specifications using AI-assisted development workflows.
3. How Domain-Driven Design can be used to separate functionality into independent bounded contexts.
4. How Apache Kafka can support asynchronous communication between microservices.
5. How Kafka Streams can process system events in real time.
6. How an LLM-based component can be integrated into a microservice architecture using LangChain4j.
7. How implementation progress can be reviewed through specification and post-implementation comparisons.

---

## Branches

The project uses specification-first and post-implementation development artifacts.

### Spec-Only Branch

Contains:

1. Specification-first artifacts in the `specs/` folder:
   - `1-technical-architecture.md` — services, layers, stack, Kafka topics and design principles
   - `2-user-stories.md` — application user stories
   - `3-domain-models.md` — entities, enums, booking state machine and event contracts
   - `4-api-endpoints.md` — REST endpoints and asynchronous event contracts
   - `openapi-contract.yaml` — OpenAPI schemas and Kafka event definitions

2. The prompt instructions in `agent_prompt.md` used during AI-assisted development.

Auxiliary files such as Maven wrappers are also included for convenience.

### Post-Implementation Branch

Contains the complete implementation:

- `catalogue-service`
- `booking-service`
- `payment-service`
- `notification-service`
- `travel-assistant-service`
- `stream-analytics-service`

Each service is an independent Maven/Spring Boot project with its own Maven wrapper.

---

## Project Structure

```text
318group-assessment/
│
├── catalogue-service/             # :8081 — packages, departures and inventory
├── booking-service/               # :8082 — booking lifecycle coordinator
├── payment-service/               # :8083 — simulated payment processing
├── notification-service/          # :8084 — booking outcome notifications
├── travel-assistant-service/      # :8085 — AI travel recommendations
├── stream-analytics-service/      # :8086 — real-time booking analytics
│
├── specs/
│   ├── 1-technical-architecture.md
│   ├── 2-user-stories.md
│   ├── 3-domain-models.md
│   ├── 4-api-endpoints.md
│   └── openapi-contract.yaml
│
├── docs/
│   └── setup-and-testing-guide.md
│
├── agent_prompt.md
├── start-all.ps1
└── README.md
```

The services generally follow a layered structure using controllers, services, repositories, models/entities and event/messaging components where appropriate.

---

## System Architecture

The application follows a microservice and event-driven architecture.

```text
                         ┌──────────────────────┐
                         │   Travel Assistant   │
                         │        :8085         │
                         │ LangChain4j + Gemini │
                         └──────────┬───────────┘
                                    │ REST
                                    ▼
┌───────────┐              ┌─────────────────┐
│  Client   │              │    Catalogue    │
└─────┬─────┘              │      :8081      │
      │                    └────────┬────────┘
      │ POST /bookings              │
      ▼                             │
┌─────────────────┐                 │
│     Booking     │◀────────────────┘
│      :8082      │
└────────┬────────┘
         │
         │ Kafka events
         ▼
┌─────────────────────────────────────────────┐
│                Apache Kafka                 │
│                    :9092                    │
└──────┬──────────────┬───────────────┬───────┘
       │              │               │
       ▼              ▼               ▼
┌────────────┐  ┌──────────────┐  ┌──────────────────┐
│  Payment   │  │ Notification │  │ Stream Analytics │
│   :8083    │  │    :8084     │  │      :8086       │
└────────────┘  └──────────────┘  │  Kafka Streams   │
                                  └──────────────────┘
```

REST is used where synchronous communication is appropriate, while Kafka events are used for the asynchronous booking lifecycle.

---

## Technology Stack

- **Java 21**
- **Spring Boot**
- **Spring Web / Spring MVC**
- **Spring Data JPA**
- **H2 Database**
- **Spring Cloud Stream**
- **Apache Kafka**
- **Kafka Streams**
- **LangChain4j**
- **Google Gemini**
- **Springdoc OpenAPI / Swagger UI**
- **JUnit 5**
- **Maven**
- **Docker Desktop**

---

## Agent-Assisted Development

The project uses an AI-assisted development workflow.

The specification-first artifacts define the intended system before implementation. The development agent can then use the instructions in `agent_prompt.md` alongside the architecture, domain model, user stories and API/event contracts.

The resulting implementation can be compared with the original specifications to review architectural decisions and development progress.

---

# Running the Implemented Version

## Prerequisites

Install:

- Java 21
- Docker Desktop
- Git
- Maven, or use the included Maven wrappers

The project uses Apache Kafka through Docker.

---

## Gemini API Configuration

The Travel Assistant requires a Google Gemini API key.

For security, the API key is **not stored in the repository**.

The application reads:

```properties
gemini.api.key=${GEMINI_API_KEY}
```

Set the environment variable on Windows PowerShell using:

```powershell
setx GEMINI_API_KEY "YOUR_API_KEY"
```

After setting the variable, open a new terminal before starting the Travel Assistant.

Never commit an API key directly into `application.properties`.

---

## Start Kafka

For the first run:

```powershell
docker run -d --name travel-kafka -p 9092:9092 apache/kafka:4.3.1
```

For subsequent runs:

```powershell
docker start travel-kafka
```

Check that Kafka is running:

```powershell
docker ps
```

---

## Start All Services Automatically

On Windows, the repository includes:

```text
start-all.ps1
```

From the repository root:

```powershell
.\start-all.ps1
```

The script starts the existing Kafka Docker container and launches all six Spring Boot services.

If PowerShell blocks script execution for the current session:

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
```

Then run:

```powershell
.\start-all.ps1
```

---

## Start Services Individually

Each service can also be started independently.

### Windows PowerShell

Open a separate terminal for each service:

```powershell
cd catalogue-service
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

```powershell
cd booking-service
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

```powershell
cd payment-service
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

```powershell
cd notification-service
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

```powershell
cd travel-assistant-service
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

```powershell
cd stream-analytics-service
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

---

## Service Addresses

| Component | Address |
| :--- | :--- |
| Apache Kafka | `localhost:9092` |
| Catalogue Service | `http://localhost:8081/api` |
| Booking Service | `http://localhost:8082/api` |
| Payment Service | `http://localhost:8083/api` |
| Notification Service | `http://localhost:8084/api` |
| Travel Assistant Service | `http://localhost:8085/api` |
| Stream Analytics Service | `http://localhost:8086/api` |

---

## Swagger / OpenAPI

Each service provides interactive API documentation using Swagger UI.

| Service | Swagger UI | OpenAPI JSON |
| :--- | :--- | :--- |
| Catalogue | `http://localhost:8081/swagger-ui/index.html` | `http://localhost:8081/v3/api-docs` |
| Booking | `http://localhost:8082/swagger-ui/index.html` | `http://localhost:8082/v3/api-docs` |
| Payment | `http://localhost:8083/swagger-ui/index.html` | `http://localhost:8083/v3/api-docs` |
| Notification | `http://localhost:8084/swagger-ui/index.html` | `http://localhost:8084/v3/api-docs` |
| Travel Assistant | `http://localhost:8085/swagger-ui/index.html` | `http://localhost:8085/v3/api-docs` |
| Stream Analytics | `http://localhost:8086/swagger-ui/index.html` | `http://localhost:8086/v3/api-docs` |

In Swagger UI:

1. Expand an endpoint.
2. Select **Try it out**.
3. Enter the required request data.
4. Select **Execute**.

The hand-written contract in `specs/openapi-contract.yaml` represents the design-time specification, while `/v3/api-docs` represents the API exposed by the running implementation.

---

# Implemented Functionality

## Catalogue Service — Port 8081

The Catalogue Service manages travel packages, departures and inventory.

Functionality includes:

- Create travel packages
- Read travel packages
- Update travel packages
- Delete travel packages
- Search packages by destination
- Create package departures
- Read package departures
- Delete package departures
- Check departure availability
- Reserve inventory
- Confirm inventory reservations
- Release inventory reservations
- Consume booking lifecycle events
- Publish inventory reservation results

The service participates directly in the asynchronous booking workflow.

---

## Booking Service — Port 8082

The Booking Service coordinates the booking lifecycle.

Functionality includes:

- Create bookings
- Read bookings
- Delete bookings
- Publish `booking-created`
- Consume `inventory-reserved`
- Consume `inventory-rejected`
- Publish `payment-requested`
- Consume `payment-completed`
- Consume `payment-failed`
- Publish `booking-confirmed`
- Publish `booking-failed`

Booking state progression:

```text
PENDING_INVENTORY
        │
        ├── inventory-reserved
        ▼
PENDING_PAYMENT
        │
        ├── payment-completed ──▶ CONFIRMED
        │
        └── payment-failed ─────▶ FAILED

inventory-rejected ──────────────▶ FAILED
```

---

## Payment Service — Port 8083

The Payment Service simulates payment processing.

Functionality includes:

- Consume `payment-requested`
- Create payment records
- Prevent duplicate processing for the same booking
- Complete valid payments
- Fail invalid payments
- Publish `payment-completed`
- Publish `payment-failed`
- Read payments by payment ID
- Read payments by booking ID

The prototype payment rule is:

```text
amount > 0  → COMPLETED
amount <= 0 → FAILED
```

---

## Notification Service — Port 8084

The Notification Service creates booking outcome notifications.

It consumes:

```text
booking-confirmed
booking-failed
```

Notification types include:

```text
BOOKING_CONFIRMED
BOOKING_FAILED
```

Functionality includes:

- Create confirmation notifications
- Create failure notifications
- Read notifications
- Read notifications by ID
- Read notifications by customer
- Read notifications by booking

---

# AI Travel Assistant — Port 8085

The Travel Assistant provides AI-assisted travel recommendations.

It uses:

- **LangChain4j**
- **Google Gemini**
- Live data from the Catalogue Service

Rather than providing the language model with a fixed list of packages, the service retrieves the current Catalogue data and provides that information to the AI model when generating a recommendation.

The recommendation endpoint is:

```text
POST /api/assistant/recommend
```

Conceptual workflow:

```text
User travel request
        │
        ▼
Travel Assistant
        │
        ├── retrieves current packages
        ▼
Catalogue Service
        │
        ▼
Travel Assistant
        │
        ├── supplies catalogue context
        ▼
Gemini via LangChain4j
        │
        ▼
Travel recommendation
```

For example, if the Catalogue contains a package named:

```text
Japan Winter Escape
```

and the user requests a winter trip to Japan, the assistant can recommend that real package rather than inventing a package that does not exist in the system.

---

# Stream Analytics — Port 8086

The Stream Analytics Service provides real-time analytics over booking events.

It uses **Kafka Streams through the Spring Cloud Stream Kafka Streams binder**.

The service processes:

```text
booking-created
booking-confirmed
booking-failed
```

It maintains analytics including:

- Total bookings
- Confirmed bookings
- Failed bookings
- Booking success rate

Analytics can be queried using:

```text
GET /api/analytics
```

Example:

```json
{
  "totalBookings": 2,
  "confirmedBookings": 1,
  "failedBookings": 1,
  "successRate": 50.0
}
```

Each stream processor uses its own Kafka Streams application ID.

The current prototype keeps the calculated counters in application memory. Therefore, the analytics counters reset when the Stream Analytics Service is restarted.

---

# Kafka Event Architecture

The booking workflow uses the following Kafka topics:

```text
booking-created
inventory-reserved
inventory-rejected
payment-requested
payment-completed
payment-failed
booking-confirmed
booking-failed
```

These events allow the services to remain independently deployable while still participating in the complete booking workflow.

---

# Successful Booking Workflow

A successful booking follows this sequence:

```text
Client
  │
  │ POST /api/bookings
  ▼
Booking Service
  │
  │ booking-created
  ▼
Kafka
  │
  ▼
Catalogue Service
  │
  │ inventory-reserved
  ▼
Kafka
  │
  ▼
Booking Service
  │
  │ status = PENDING_PAYMENT
  │ payment-requested
  ▼
Kafka
  │
  ▼
Payment Service
  │
  │ payment-completed
  ▼
Kafka
  │
  ▼
Booking Service
  │
  │ status = CONFIRMED
  │ booking-confirmed
  ▼
Kafka
  ├──────────────▶ Catalogue Service
  │                 reservation CONFIRMED
  │
  ├──────────────▶ Notification Service
  │                 BOOKING_CONFIRMED
  │
  └──────────────▶ Stream Analytics
                    confirmedBookings + 1
```

Stream Analytics also consumes `booking-created`, increasing the total booking count.

---

# Failed Booking Workflow

The system also supports failure and compensation.

For example, if insufficient inventory exists:

```text
Booking Service
      │
      │ booking-created
      ▼
Catalogue Service
      │
      │ inventory-rejected
      ▼
Booking Service
      │
      ▼
    FAILED
      │
      │ booking-failed
      ▼
     Kafka
      ├────────────▶ Notification Service
      │               BOOKING_FAILED
      │
      └────────────▶ Stream Analytics
                      failedBookings + 1
```

Payment failure is also supported.

If payment fails after inventory has been reserved:

```text
Payment Service
      │
      │ payment-failed
      ▼
Booking Service
      │
      ▼
    FAILED
      │
      │ booking-failed
      ▼
Catalogue Service
      │
      ▼
Reservation RELEASED
```

This provides compensating behaviour so reserved inventory is returned when the booking cannot be completed.

---

# End-to-End Testing

A complete integration test can be performed using Swagger UI or an API client such as Thunder Client.

## Example Successful Test

### 1. Create a package

```text
POST http://localhost:8081/api/packages
```

Example:

```json
{
  "name": "Japan Winter Escape",
  "destination": "Japan",
  "description": "A 7-day winter holiday in Japan for two travellers, including Tokyo and Kyoto."
}
```

### 2. Create a departure

Assuming the package ID is `1`:

```text
POST http://localhost:8081/api/packages/1/departures
```

Example:

```json
{
  "startDate": "2026-12-10",
  "endDate": "2026-12-17",
  "price": 4500.00,
  "capacity": 20,
  "status": "AVAILABLE"
}
```

### 3. Create a booking

Assuming the departure ID is `1`:

```text
POST http://localhost:8082/api/bookings
```

Example:

```json
{
  "customerId": "11111111-1111-1111-1111-111111111111",
  "packageId": 1,
  "departureId": 1,
  "quantity": 2,
  "totalPrice": 9000.00
}
```

The event-driven workflow should automatically reserve inventory, process payment, confirm the booking, create a notification and update Stream Analytics.

### 4. Verify Booking

```text
GET http://localhost:8082/api/bookings
```

Expected final state:

```text
CONFIRMED
```

### 5. Verify Payment

```text
GET http://localhost:8083/api/payments
```

Expected:

```text
COMPLETED
```

### 6. Verify Notification

```text
GET http://localhost:8084/api/notifications
```

Expected notification type:

```text
BOOKING_CONFIRMED
```

### 7. Verify Analytics

```text
GET http://localhost:8086/api/analytics
```

After one successful booking:

```json
{
  "totalBookings": 1,
  "confirmedBookings": 1,
  "failedBookings": 0,
  "successRate": 100.0
}
```

---

# Failure Testing

Inventory failure can be tested by requesting a quantity greater than the available departure capacity.

For example:

```json
{
  "customerId": "33333333-3333-3333-3333-333333333333",
  "packageId": 1,
  "departureId": 1,
  "quantity": 100,
  "totalPrice": 450000.00
}
```

The booking should eventually reach:

```text
FAILED
```

After one successful booking and one failed booking, Stream Analytics should report:

```json
{
  "totalBookings": 2,
  "confirmedBookings": 1,
  "failedBookings": 1,
  "successRate": 50.0
}
```

---

# Automated Testing

Each service includes automated tests appropriate to its responsibilities.

Tests can be run independently using:

```powershell
.\mvnw.cmd clean test
```

The Stream Analytics tests verify:

- Initial analytics values
- Booking-created counting
- Booking-confirmed counting
- Booking-failed counting
- Success-rate calculation
- Analytics REST controller output

The Travel Assistant tests use mocked dependencies so automated tests do not require a live Gemini API call.

---

# Security

Secrets such as the Gemini API key must not be stored directly in source control.

The Travel Assistant reads the key from:

```text
GEMINI_API_KEY
```

The repository `.gitignore` excludes common environment, IDE and build files including:

```text
.env
target/
.vscode/
.idea/
*.log
```

If a secret is accidentally committed, the key should be revoked immediately and removed from Git history before pushing.

---

# Current Implementation Status

| Component | Status |
| :--- | :---: |
| Catalogue Service | ✅ |
| Booking Service | ✅ |
| Payment Service | ✅ |
| Notification Service | ✅ |
| Travel Assistant Service | ✅ |
| Stream Analytics Service | ✅ |
| Kafka event workflow | ✅ |
| Successful booking workflow | ✅ |
| Failed booking workflow | ✅ |
| Inventory compensation | ✅ |
| AI Catalogue integration | ✅ |
| Kafka Streams processing | ✅ |
| Automated testing | ✅ |
| Swagger / OpenAPI | ✅ |

The implemented application therefore demonstrates a complete microservice-based, event-driven travel booking workflow with AI-assisted recommendations and real-time stream analytics.