> Detailed setup, manual integration-testing and troubleshooting guide. For the project overview, specifications and quick-start see the root [README.md](../README.md) and the [specs/](../specs/) folder.

# AI-Assisted Travel Booking System

A microservices-based travel booking application developed using Spring Boot.

The system is designed around independent services that communicate asynchronously using Apache Kafka. The current implementation includes the **Catalogue Service** and **Booking Service**, which together manage travel packages, departures, inventory reservations, booking creation, and booking lifecycle events.

The architecture is designed so that additional services such as Payment, Customer, AI/Recommendation, and Notification services can be integrated independently.

---

# 1. Current Services

## Catalogue Service

The Catalogue Service manages:

- Travel packages
- Package destinations
- Package departures
- Departure capacity
- Inventory availability
- Inventory reservations
- Reservation confirmation
- Reservation release

The Catalogue Service runs on:

```text
http://localhost:8081
```
## Booking Service

The Booking Service manages:

- Booking creation
- Booking retrieval
- Booking deletion
- Booking lifecycle state
- Inventory reservation results
- Payment requests
- Payment results
- Final booking confirmation/failure

The Booking Service runs on:

```text
http://localhost:8082
```

## Apache Kafka

Apache Kafka provides asynchronous communication between the microservices.

Kafka runs on:

```text
localhost:9092
```

---

# 2. Technology Stack

The current implementation uses:

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Validation
- Spring Cloud Stream
- Apache Kafka
- H2 Database
- Maven
- JUnit 5
- Mockito
- MockMvc
- Docker
- Thunder Client for manual API testing

Each microservice owns its own database and communicates with other services through events rather than directly sharing database tables.

---

# 3. Project Structure

```text
318group-assessment/
│
├── catalogue-service/                 # :8081 — packages, departures, inventory
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/travelbooking/catalogue/
│   │   │   │   ├── controller/
│   │   │   │   ├── event/
│   │   │   │   ├── exception/
│   │   │   │   ├── messaging/
│   │   │   │   ├── model/
│   │   │   │   ├── repository/
│   │   │   │   └── service/
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   │       └── java/com/travelbooking/catalogue/
│   ├── pom.xml
│   ├── mvnw
│   └── mvnw.cmd
│
├── booking-service/                   # :8082 — booking lifecycle coordinator
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/travelbooking/booking/
│   │   │   │   ├── controller/
│   │   │   │   ├── event/
│   │   │   │   ├── exception/
│   │   │   │   ├── messaging/
│   │   │   │   ├── model/
│   │   │   │   ├── repository/
│   │   │   │   └── service/
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   │       └── java/com/travelbooking/booking/
│   ├── pom.xml
│   ├── mvnw
│   └── mvnw.cmd
│
├── payment-service/                   # :8083 — simulated payment gateway
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/travelbooking/payment/
│   │   │   │   ├── controller/
│   │   │   │   ├── event/
│   │   │   │   ├── messaging/
│   │   │   │   ├── model/
│   │   │   │   ├── repository/
│   │   │   │   └── service/
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   │       └── java/com/travelbooking/payment/
│   ├── pom.xml
│   ├── mvnw
│   └── mvnw.cmd
│
├── notification-service/              # :8084 — booking-outcome notifications
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/travelbooking/notification/
│   │   │   │   ├── controller/
│   │   │   │   ├── event/
│   │   │   │   ├── messaging/
│   │   │   │   ├── model/
│   │   │   │   ├── repository/
│   │   │   │   └── service/
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   │       └── java/com/travelbooking/notification/
│   ├── pom.xml
│   ├── mvnw
│   └── mvnw.cmd
│
├── specs/                             # specification-first artifacts
│   ├── 1-technical-architecture.md
│   ├── 2-user-stories.md
│   ├── 3-domain-models.md
│   ├── 4-api-endpoints.md
│   └── openapi-contract.yaml
│
├── docs/
│   └── setup-and-testing-guide.md     # this file
│
├── agent_prompt.md                    # instructions for the development agent
└── README.md                          # project overview and quick start
```

---

# 4. Architecture

The application follows a microservices architecture.

```text
                     ┌──────────────────────┐
                     │    Booking Service   │
                     │        :8082         │
                     └──────────┬───────────┘
                                │
                         BookingCreated
                                │
                                ▼
                     ┌──────────────────────┐
                     │    Apache Kafka      │
                     │        :9092         │
                     └──────────┬───────────┘
                                │
                                ▼
                     ┌──────────────────────┐
                     │  Catalogue Service   │
                     │        :8081         │
                     └──────────┬───────────┘
                                │
                  InventoryReserved / Rejected
                                │
                                ▼
                     ┌──────────────────────┐
                     │    Apache Kafka      │
                     └──────────┬───────────┘
                                │
                                ▼
                     ┌──────────────────────┐
                     │    Booking Service   │
                     └──────────────────────┘
```

The Booking Service coordinates the booking lifecycle while the Catalogue Service owns package, departure and inventory information.

---

# 5. Requirements

Before running the application, install:

## Java

Java 21 is required.

Check the installed version:

```powershell
java --version
```

Expected:

```text
openjdk 21
```

## Docker Desktop

Docker Desktop is required to run Kafka.

Check Docker:

```powershell
docker --version
```

Then make sure Docker Desktop is running.

Check the Docker engine:

```powershell
docker info
```

## Maven

The project includes the Maven Wrapper, so a separate Maven installation is not required.

Commands can be run using:

```powershell
.\mvnw.cmd
```

---

# 6. Starting Apache Kafka

Start Docker Desktop first.

The Kafka container can then be created using:

```powershell
docker run -d --name travel-kafka -p 9092:9092 apache/kafka:4.3.1
```

The first execution may take some time because Docker must download the Kafka image.

Check the container:

```powershell
docker ps
```

You should see:

```text
travel-kafka
```

with port:

```text
9092
```

## Starting Kafka Again Later

The `docker run` command only needs to be used when creating the container for the first time.

If the container already exists but is stopped:

```powershell
docker start travel-kafka
```

Then check:

```powershell
docker ps
```

## Stopping Kafka

```powershell
docker stop travel-kafka
```

---

# 7. Running the Catalogue Service

Open a new terminal.

From the project root:

```powershell
cd catalogue-service
```

Start the service:

```powershell
.\mvnw.cmd spring-boot:run
```

The service should start on:

```text
http://localhost:8081
```

Do not close this terminal while testing the system.

---

# 8. Running the Booking Service

Open another terminal.

From the project root:

```powershell
cd booking-service
```

Start the service:

```powershell
.\mvnw.cmd spring-boot:run
```

The service should start on:

```text
http://localhost:8082
```

Do not close this terminal while testing.

---

# 9. Running the Complete System

For integration testing, the following three components should be running simultaneously:

```text
Kafka               localhost:9092
Catalogue Service   localhost:8081
Booking Service     localhost:8082
```

A typical setup therefore uses:

```text
Docker Desktop
│
└── Kafka :9092

Terminal 1
└── Catalogue Service :8081

Terminal 2
└── Booking Service :8082
```

---

# 10. Catalogue API

Base URL:

```text
http://localhost:8081
```

## Get All Travel Packages

```http
GET /api/packages
```

Example:

```text
GET http://localhost:8081/api/packages
```

---

## Search Packages by Destination

```http
GET /api/packages?destination=Tokyo
```

Example:

```text
GET http://localhost:8081/api/packages?destination=Tokyo
```

---

## Get Package by ID

```http
GET /api/packages/{packageId}
```

Example:

```text
GET http://localhost:8081/api/packages/1
```

---

## Create Travel Package

```http
POST /api/packages
Content-Type: application/json
```

Example request:

```json
{
  "name": "Tokyo Adventure",
  "destination": "Tokyo, Japan",
  "description": "Seven-day holiday package exploring Tokyo."
}
```

Example URL:

```text
POST http://localhost:8081/api/packages
```

---

## Update Travel Package

```http
PUT /api/packages/{packageId}
Content-Type: application/json
```

Example:

```text
PUT http://localhost:8081/api/packages/1
```

Example body:

```json
{
  "name": "Tokyo Premium Adventure",
  "destination": "Tokyo, Japan",
  "description": "Premium seven-day holiday package exploring Tokyo."
}
```

---

## Delete Travel Package

```http
DELETE /api/packages/{packageId}
```

Example:

```text
DELETE http://localhost:8081/api/packages/1
```

---

# 11. Departure API

## Create Departure

```http
POST /api/packages/{packageId}/departures
Content-Type: application/json
```

Example:

```text
POST http://localhost:8081/api/packages/1/departures
```

Body:

```json
{
  "startDate": "2026-12-01",
  "endDate": "2026-12-08",
  "price": 2199.99,
  "capacity": 20
}
```

---

## Check Departure Availability

```http
GET /api/departures/{departureId}/availability
```

Example:

```text
GET http://localhost:8081/api/departures/1/availability
```

For a departure with capacity 20 and no reservations, the available capacity should be:

```json
{
  "availableCapacity": 20
}
```

---

# 12. Booking API

Base URL:

```text
http://localhost:8082
```

## Get All Bookings

```http
GET /api/bookings
```

Example:

```text
GET http://localhost:8082/api/bookings
```

---

## Get Booking by ID

```http
GET /api/bookings/{bookingId}
```

Example:

```text
GET http://localhost:8082/api/bookings/c3d2952d-da15-4c49-808d-4bcbe0beb8fd
```

---

## Create Booking

```http
POST /api/bookings
Content-Type: application/json
```

Example:

```text
POST http://localhost:8082/api/bookings
```

Body:

```json
{
  "customerId": "123e4567-e89b-12d3-a456-426614174000",
  "packageId": 1,
  "departureId": 1,
  "quantity": 2,
  "totalPrice": 4399.98
}
```

When initially created, the booking is placed into:

```text
PENDING_INVENTORY
```

The Booking Service then publishes a `BookingCreated` event.

---

## Delete Booking

```http
DELETE /api/bookings/{bookingId}
```

Example:

```text
DELETE http://localhost:8082/api/bookings/c3d2952d-da15-4c49-808d-4bcbe0beb8fd
```

---

# 13. Booking Lifecycle

The Booking Service uses states to control the booking lifecycle.

```text
                     ┌───────────────────┐
                     │ PENDING_INVENTORY │
                     └─────────┬─────────┘
                               │
                  InventoryReserved
                               │
                               ▼
                     ┌───────────────────┐
                     │  PENDING_PAYMENT  │
                     └─────────┬─────────┘
                               │
                 ┌─────────────┴─────────────┐
                 │                           │
          PaymentCompleted              PaymentFailed
                 │                           │
                 ▼                           ▼
          ┌─────────────┐              ┌──────────┐
          │  CONFIRMED  │              │  FAILED  │
          └─────────────┘              └──────────┘
```

Inventory rejection also causes:

```text
PENDING_INVENTORY
        ↓
InventoryRejected
        ↓
FAILED
```

Booking state is controlled by events rather than a public endpoint that manually changes booking status.

---

# 14. Kafka Event Flow

## Successful Booking

```text
Client
  │
  │ POST /api/bookings
  ▼
Booking Service
  │
  │ BookingCreated
  ▼
Kafka
  │
  ▼
Catalogue Service
  │
  │ Check capacity
  │ Create reservation
  │
  │ InventoryReserved
  ▼
Kafka
  │
  ▼
Booking Service
  │
  │ reservationId saved
  │ PENDING_PAYMENT
  │
  │ PaymentRequested
  ▼
Kafka
  │
  ▼
Payment Service
  │
  │ PaymentCompleted
  ▼
Kafka
  │
  ▼
Booking Service
  │
  │ paymentId saved
  │ CONFIRMED
  │
  │ BookingConfirmed
  ▼
Kafka
  │
  ▼
Catalogue Service
  │
  ▼
Reservation CONFIRMED
```

---

# 15. Failed Payment Flow

If payment fails:

```text
Booking
   │
   │ PaymentRequested
   ▼
Payment Service
   │
   │ PaymentFailed
   ▼
Booking Service
   │
   ├── status = FAILED
   │
   └── BookingFailed
           │
           ▼
         Kafka
           │
           ▼
    Catalogue Service
           │
           ▼
Reservation RELEASED
           │
           ▼
Capacity restored
```

This prevents failed bookings from permanently consuming travel inventory.

---

# 16. Inventory Capacity Logic

Catalogue calculates available capacity using active reservations.

Reservations with the following states consume capacity:

```text
RESERVED
CONFIRMED
```

Released reservations no longer consume capacity.

For example:

```text
Departure capacity = 20

Booking A reserves 2
Available = 18

Booking B reserves 3
Available = 15

Booking B payment fails
Reservation released
Available = 18
```

This behaviour has been tested successfully using the running Kafka integration.

---

# 17. Kafka Topics

The current workflow uses the following Kafka topics:

| Topic | Producer | Consumer |
|---|---|---|
| `booking-created` | Booking | Catalogue |
| `inventory-reserved` | Catalogue | Booking |
| `inventory-rejected` | Catalogue | Booking |
| `payment-requested` | Booking | Payment |
| `payment-completed` | Payment | Booking |
| `payment-failed` | Payment | Booking |
| `booking-confirmed` | Booking | Catalogue |
| `booking-failed` | Booking | Catalogue |

Kafka is configured at:

```text
localhost:9092
```

---

# 18. Testing Without the Payment Service

The Payment Service can be simulated manually using Kafka while it is still being developed or integrated.

## Simulate Successful Payment

First obtain the booking ID:

```text
GET http://localhost:8082/api/bookings
```

The booking should currently have:

```json
{
  "paymentId": null,
  "status": "PENDING_PAYMENT"
}
```

Open a terminal and run:

```powershell
docker exec -it travel-kafka /opt/kafka/bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic payment-completed
```

Enter:

```json
{"bookingId":"YOUR-BOOKING-ID","paymentId":"550e8400-e29b-41d4-a716-446655440001"}
```

Press Enter.

Then check:

```text
GET http://localhost:8082/api/bookings
```

The booking should now contain:

```json
{
  "paymentId": "550e8400-e29b-41d4-a716-446655440001",
  "status": "CONFIRMED"
}
```

---

# 19. Simulating Payment Failure

Create another booking and wait for it to reach:

```text
PENDING_PAYMENT
```

Then run:

```powershell
docker exec -it travel-kafka /opt/kafka/bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic payment-failed
```

Enter:

```json
{"bookingId":"YOUR-BOOKING-ID","paymentId":"550e8400-e29b-41d4-a716-446655440002","reason":"Payment declined"}
```

Press Enter.

The booking should change to:

```text
FAILED
```

The Catalogue Service should receive `BookingFailed` and release the associated reservation.

---

# 20. Complete Manual Integration Test

The following workflow can be used to demonstrate the two services.

## Step 1 — Start Kafka

```powershell
docker start travel-kafka
```

Check:

```powershell
docker ps
```

---

## Step 2 — Start Catalogue

```powershell
cd catalogue-service
.\mvnw.cmd spring-boot:run
```

---

## Step 3 — Start Booking

```powershell
cd booking-service
.\mvnw.cmd spring-boot:run
```

---

## Step 4 — Create Package

```text
POST http://localhost:8081/api/packages
```

```json
{
  "name": "Tokyo Adventure",
  "destination": "Tokyo, Japan",
  "description": "Seven-day holiday package exploring Tokyo."
}
```

---

## Step 5 — Create Departure

```text
POST http://localhost:8081/api/packages/1/departures
```

```json
{
  "startDate": "2026-12-01",
  "endDate": "2026-12-08",
  "price": 2199.99,
  "capacity": 20
}
```

---

## Step 6 — Check Initial Capacity

```text
GET http://localhost:8081/api/departures/1/availability
```

Expected:

```json
{
  "availableCapacity": 20
}
```

---

## Step 7 — Create Booking

```text
POST http://localhost:8082/api/bookings
```

```json
{
  "customerId": "123e4567-e89b-12d3-a456-426614174000",
  "packageId": 1,
  "departureId": 1,
  "quantity": 2,
  "totalPrice": 4399.98
}
```

---

## Step 8 — Check Booking

```text
GET http://localhost:8082/api/bookings
```

After Kafka processes the inventory events, the booking should have:

```text
reservationId = populated
status = PENDING_PAYMENT
paymentId = null
```

---

## Step 9 — Check Capacity

```text
GET http://localhost:8081/api/departures/1/availability
```

Expected:

```json
{
  "availableCapacity": 18
}
```

This demonstrates that Booking and Catalogue have communicated asynchronously through Kafka.

---

## Step 10 — Simulate Payment

Publish a `payment-completed` event.

After processing, the booking should show:

```text
status = CONFIRMED
paymentId = populated
```

The Catalogue reservation should also become confirmed.

---

# 21. Automated Tests

The project includes automated tests for both services.

## Catalogue Tests

Catalogue tests cover important inventory business rules including:

- Available capacity calculation
- Successful inventory reservation
- Insufficient capacity
- Duplicate booking protection
- Missing departure
- Unavailable departure
- Reservation confirmation
- Reservation release
- REST controller behaviour
- Valid and invalid HTTP requests

Run:

```powershell
cd catalogue-service
.\mvnw.cmd clean test
```

Successful execution should finish with:

```text
BUILD SUCCESS
```

---

## Booking Tests

Booking tests cover:

- Booking creation
- Initial `PENDING_INVENTORY` state
- `InventoryReserved`
- `InventoryRejected`
- `PaymentCompleted`
- `PaymentFailed`
- Invalid state/event protection
- Kafka event publication
- REST controller behaviour
- 404 responses
- Validation
- Booking deletion

Run:

```powershell
cd booking-service
.\mvnw.cmd clean test
```

Successful execution should finish with:

```text
BUILD SUCCESS
```

---

# 22. Error Handling and Validation

Both services include global exception handling.

Invalid request bodies return HTTP:

```text
400 Bad Request
```

Unknown resources return:

```text
404 Not Found
```

Unexpected server-side errors return:

```text
500 Internal Server Error
```

Validation is applied to incoming request bodies using Jakarta Validation and Spring's `@Valid` support.

---

# 23. Database Design

Each microservice owns its own persistence layer.

```text
Catalogue Service
      │
      └── Catalogue Database

Booking Service
      │
      └── Booking Database
```

The services do not directly access each other's database.

This maintains service independence and prevents tight coupling between the microservices.

H2 is currently used for development and testing.

---

# 24. Event-Driven Design

The system uses asynchronous Kafka events instead of synchronous REST calls for the booking lifecycle.

This provides several benefits:

- Reduced coupling between services
- Services can evolve independently
- Booking does not need direct access to Catalogue data
- Catalogue does not need direct access to Booking data
- Payment can be integrated independently
- Events represent important business state changes

The Booking Service acts as the coordinator of the overall booking lifecycle.

---

# 25. Reservation Protection

Catalogue prevents the same booking from creating multiple inventory reservations.

Reservations are associated with a unique booking ID.

This helps protect against duplicate Kafka messages or repeated reservation requests.

Booking also checks its current state before processing lifecycle events.

For example:

```text
InventoryReserved
```

is only processed when the booking is:

```text
PENDING_INVENTORY
```

and:

```text
PaymentCompleted
```

is only processed when the booking is:

```text
PENDING_PAYMENT
```

This prevents repeated or out-of-order events from incorrectly advancing the booking lifecycle.

---

# 26. Current Integration Status

The following integration has been implemented and manually verified:

```text
Booking
   ↓
BookingCreated
   ↓
Kafka
   ↓
Catalogue
   ↓
InventoryReserved
   ↓
Kafka
   ↓
Booking
   ↓
PENDING_PAYMENT
```

The successful payment path has also been tested:

```text
PaymentCompleted
   ↓
Booking CONFIRMED
   ↓
BookingConfirmed
   ↓
Catalogue reservation CONFIRMED
```

The failed payment path has been tested:

```text
PaymentFailed
   ↓
Booking FAILED
   ↓
BookingFailed
   ↓
Catalogue reservation RELEASED
   ↓
Capacity restored
```

Payment events were manually produced through Kafka during testing while the external Payment Service was not connected.

---

# 27. Future Service Integration

The architecture allows additional microservices to be connected without changing the core Catalogue and Booking responsibilities.

Potential services include:

```text
Customer Service
Payment Service
AI / Recommendation Service
Notification Service
API Gateway
```

The Payment Service can consume:

```text
payment-requested
```

and publish either:

```text
payment-completed
```

or:

```text
payment-failed
```

without requiring direct database access to Booking or Catalogue.

---

# 28. Useful Commands

## Check Docker

```powershell
docker info
```

## Check Kafka Container

```powershell
docker ps
```

## Start Existing Kafka Container

```powershell
docker start travel-kafka
```

## Stop Kafka

```powershell
docker stop travel-kafka
```

## Start Catalogue

```powershell
cd catalogue-service
.\mvnw.cmd spring-boot:run
```

## Start Booking

```powershell
cd booking-service
.\mvnw.cmd spring-boot:run
```

## Test Catalogue

```powershell
cd catalogue-service
.\mvnw.cmd clean test
```

## Test Booking

```powershell
cd booking-service
.\mvnw.cmd clean test
```

## Build Catalogue

```powershell
cd catalogue-service
.\mvnw.cmd clean package
```

## Build Booking

```powershell
cd booking-service
.\mvnw.cmd clean package
```

---

# 29. Troubleshooting

## Docker command works but Kafka will not start

If:

```powershell
docker --version
```

works but Docker reports that it cannot connect to:

```text
dockerDesktopLinuxEngine
```

open **Docker Desktop** and wait for the Docker engine to start.

Then run:

```powershell
docker info
```

---

## Kafka container already exists

If Docker reports that `travel-kafka` already exists, do not create another container.

Run:

```powershell
docker start travel-kafka
```

---

## Port Already in Use

The services require:

```text
8081 - Catalogue
8082 - Booking
9092 - Kafka
```

Make sure another application is not already using these ports.

---

## Booking Stays at PENDING_INVENTORY

Check:

1. Kafka is running.
2. Catalogue Service is running.
3. Both services point to `localhost:9092`.
4. Catalogue has subscribed to `booking-created`.
5. Check the Catalogue terminal for received Kafka events.

---

## Booking Stays at PENDING_PAYMENT

This is expected if no Payment Service is connected.

Either connect the Payment Service or manually publish a test event to:

```text
payment-completed
```

or:

```text
payment-failed
```

---

# 30. Summary

The current implementation provides a working event-driven foundation for the AI-assisted travel booking system.

The Catalogue and Booking services currently demonstrate:

- Independent Spring Boot microservices
- Independent persistence
- REST APIs
- Travel package management
- Departure management
- Inventory management
- Capacity protection
- Booking lifecycle management
- Apache Kafka integration
- Asynchronous inter-service communication
- Inventory reservation and release
- Payment-event integration
- Successful and failed booking workflows
- Request validation
- Global error handling
- Automated service tests
- Automated controller tests
- Manual end-to-end integration testing

The successful and failed booking workflows have both been verified using the running Catalogue Service, Booking Service, Apache Kafka, and simulated payment events.