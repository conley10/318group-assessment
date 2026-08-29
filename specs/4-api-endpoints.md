# API Endpoints Summary

All services expose JSON REST APIs under the `/api` prefix. Request bodies are validated; validation failures return `400 Bad Request` with a `{ "<field>": "<message>" }` body, unknown resources return `404 Not Found`, and unexpected errors return `500 Internal Server Error` with `{ "message": "An unexpected error occurred" }`.

## 1. Catalogue Service (`http://localhost:8081/api`)

### 1.1 Travel Packages

| Feature | Method | Endpoint Path | Request Body | Response (Success) | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **C2** | `GET` | `/packages` | *None* | `200 OK` (`Array<TravelPackage>`) | List all travel packages |
| **C2** | `GET` | `/packages?destination={text}` | *None* | `200 OK` (`Array<TravelPackage>`) | Search packages whose destination contains `text` (case-insensitive) |
| **C1** | `GET` | `/packages/{packageId}` | *None* | `200 OK` (`TravelPackage`) | Get package details by ID |
| **C1** | `POST` | `/packages` | `TravelPackageRequest` | `201 Created` (`TravelPackage`, `Location` header) | Create a new travel package |
| **C1** | `PUT` | `/packages/{packageId}` | `TravelPackageRequest` | `200 OK` (`TravelPackage`) | Update name, destination and description |
| **C1** | `DELETE` | `/packages/{packageId}` | *None* | `204 No Content` | Delete a package by ID |

### 1.2 Package Departures

| Feature | Method | Endpoint Path | Request Body | Response (Success) | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **C3** | `GET` | `/packages/{packageId}/departures` | *None* | `200 OK` (`Array<PackageDeparture>`) | List departures of a package |
| **C3** | `POST` | `/packages/{packageId}/departures` | `PackageDepartureRequest` | `201 Created` (`PackageDeparture`, `Location` header) | Add a departure to a package (`404` if package unknown) |
| **C3** | `GET` | `/departures/{departureId}` | *None* | `200 OK` (`PackageDeparture`) | Get a departure by ID |
| **C3** | `DELETE` | `/departures/{departureId}` | *None* | `204 No Content` | Delete a departure by ID |
| **C4** | `GET` | `/departures/{departureId}/availability` | *None* | `200 OK` (`AvailabilityResponse`) | Available places on the departure (`404` if unknown) |

### 1.3 Inventory Reservations

| Feature | Method | Endpoint Path | Request Body | Response (Success) | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **C5** | `GET` | `/departures/{departureId}/reservations` | *None* | `200 OK` (`Array<InventoryReservation>`) | List reservations on a departure |
| **C5** | `POST` | `/departures/{departureId}/reservations` | `InventoryReservationRequest` | `201 Created` (`InventoryReservation`, `Location` header) | Reserve places; `404` if departure unknown, `409 Conflict` (`MessageResponse`) if unavailable / insufficient capacity / duplicate booking |
| **C5** | `GET` | `/reservations/{reservationId}` | *None* | `200 OK` (`InventoryReservation`) | Get a reservation by ID |
| **C5** | `PUT` | `/reservations/{reservationId}/confirm` | *None* | `200 OK` (`InventoryReservation`) | Set reservation status to `CONFIRMED` |
| **C5** | `PUT` | `/reservations/{reservationId}/release` | *None* | `200 OK` (`InventoryReservation`) | Set reservation status to `RELEASED` (restores capacity) |

### 1.4 Health

| Method | Endpoint Path | Response | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/` (no `/api` prefix) | `200 OK` (`text/plain` "Catalogue Service is running") | Simple liveness check |

---

## 2. Booking Service (`http://localhost:8082/api`)

| Feature | Method | Endpoint Path | Request Body | Response (Success) | Inter-Service Event Dependencies |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **B2** | `GET` | `/bookings` | *None* | `200 OK` (`Array<Booking>`) | None |
| **B2** | `GET` | `/bookings/{bookingId}` | *None* | `200 OK` (`Booking`) | None |
| **B1** | `POST` | `/bookings` | `BookingCreateRequest` | `201 Created` (`Booking`, status `PENDING_INVENTORY`, `Location` header) | **Publishes** `booking-created` → Catalogue Service |
| **B3** | `DELETE` | `/bookings/{bookingId}` | *None* | `204 No Content` | None |

Booking status is **not** changeable through the REST API; it advances only via the events in section 5.

---

## 3. Payment Service (`http://localhost:8083/api`)

| Feature | Method | Endpoint Path | Request Body | Response (Success) | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **P2** | `GET` | `/payments` | *None* | `200 OK` (`Array<Payment>`) | List all payments |
| **P2** | `GET` | `/payments/{paymentId}` | *None* | `200 OK` (`Payment`) | Get a payment by ID |
| **P2** | `GET` | `/payments/booking/{bookingId}` | *None* | `200 OK` (`Payment`) | Get the payment for a booking |

Payments are created only by consuming `payment-requested` events (**P1**); there is no `POST` endpoint.

---

## 4. Notification Service (`http://localhost:8084/api`)

| Feature | Method | Endpoint Path | Request Body | Response (Success) | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **N2** | `GET` | `/notifications` | *None* | `200 OK` (`Array<Notification>`) | List all notifications |
| **N2** | `GET` | `/notifications/{notificationId}` | *None* | `200 OK` (`Notification`) | Get a notification by ID |
| **N2** | `GET` | `/notifications/customer/{customerId}` | *None* | `200 OK` (`Array<Notification>`) | List notifications for a customer (empty list if none) |
| **N2** | `GET` | `/notifications/booking/{bookingId}` | *None* | `200 OK` (`Array<Notification>`) | List notifications for a booking |

Notifications are created only by consuming `booking-confirmed` / `booking-failed` events (**N1**); there is no `POST` endpoint.

---

## 5. Asynchronous Event Contracts (Apache Kafka `localhost:9092`)

| Feature | Topic | Producer | Consumer(s) | Payload | Consumer behaviour |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **B1 → C5** | `booking-created` | Booking | Catalogue | `BookingCreatedEvent` | Reserve inventory; publish `inventory-reserved` or `inventory-rejected` |
| **C5 → B4** | `inventory-reserved` | Catalogue | Booking | `InventoryReservedEvent` | `PENDING_INVENTORY → PENDING_PAYMENT`; publish `payment-requested` |
| **C5 → B4** | `inventory-rejected` | Catalogue | Booking | `InventoryRejectedEvent` | `PENDING_INVENTORY → FAILED` |
| **B4 → P1** | `payment-requested` | Booking | Payment | `PaymentRequestedEvent` | Create payment; publish `payment-completed` or `payment-failed` |
| **P1 → B4** | `payment-completed` | Payment | Booking | `PaymentCompletedEvent` | `PENDING_PAYMENT → CONFIRMED`; publish `booking-confirmed` |
| **P1 → B4** | `payment-failed` | Payment | Booking | `PaymentFailedEvent` | `PENDING_PAYMENT → FAILED`; publish `booking-failed` |
| **B4 → C5, N1** | `booking-confirmed` | Booking | Catalogue, Notification | `BookingConfirmedEvent` | Catalogue: reservation → `CONFIRMED`; Notification: create `BOOKING_CONFIRMED` |
| **B4 → C5, N1** | `booking-failed` | Booking | Catalogue, Notification | `BookingFailedEvent` | Catalogue: reservation → `RELEASED`; Notification: create `BOOKING_FAILED` |

All messages are JSON with the field names defined in `3-domain-models.md` §6. Spring Cloud Stream binding names follow the pattern `<eventName>-out-0` (producer) / `<eventName>-in-0` (consumer).

Full request/response schemas are defined in `openapi-contract.yaml`.
