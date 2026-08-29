# User Stories & Specifications

## Roles
- **Customer** — browses travel packages, creates bookings, pays, and receives notifications.
- **Administrator** — a travel-agency staff member who maintains the catalogue (packages, departures, capacity) and can inspect bookings, payments and notifications.
- **System** — automated behaviour triggered by Kafka events rather than by a user request.

## User Story Overview

| Service | User Story ID | User Story Name | Role(s) |
| :--- | :--- | :--- | :--- |
| **Catalogue Service** | C1 | Create/get/update/delete a Travel Package | Administrator, Customer (read) |
| **Catalogue Service** | C2 | List and Search Travel Packages by Destination | Customer, Administrator |
| **Catalogue Service** | C3 | Manage Package Departures | Administrator, Customer (read) |
| **Catalogue Service** | C4 | Check Departure Availability | Customer, Administrator |
| **Catalogue Service** | C5 | Reserve, Confirm and Release Inventory | System, Administrator |
| **Booking Service** | B1 | Create a Booking | Customer |
| **Booking Service** | B2 | Get/list Bookings | Customer, Administrator |
| **Booking Service** | B3 | Delete a Booking | Customer, Administrator |
| **Booking Service** | B4 | Booking Lifecycle driven by Events | System |
| **Payment Service** | P1 | Process Payment for a Booking | System |
| **Payment Service** | P2 | View Payments | Customer, Administrator |
| **Notification Service** | N1 | Notify Customer of Booking Outcome | System |
| **Notification Service** | N2 | View Notifications | Customer, Administrator |

---

## Catalogue Service

### User Story C1: Create/get/update/delete a Travel Package
- **As an** Administrator, **I want to** create a new travel package with a name, destination and description.
- **As a** Customer / Administrator, **I want to** get the details of a travel package by its ID.
- **As an** Administrator, **I want to** update the name, destination and description of an existing travel package by its ID.
- **As an** Administrator, **I want to** delete a travel package by its ID.
- **Acceptance**: name and destination are required and at most 100 characters; description is required and at most 500 characters; invalid input returns `400`; unknown IDs return `404`.

### User Story C2: List and Search Travel Packages by Destination
- **As a** Customer / Administrator, **I want to** retrieve a list of all travel packages.
- **As a** Customer / Administrator, **I want to** search packages by (partial, case-insensitive) destination, e.g. `?destination=Tokyo`.

### User Story C3: Manage Package Departures
- **As an** Administrator, **I want to** add a departure (start date, end date, price, capacity) to a travel package.
- **As a** Customer / Administrator, **I want to** list all departures for a package and get a departure by its ID.
- **As an** Administrator, **I want to** delete a departure by its ID.
- **Acceptance**: start and end dates are required; price must be greater than zero; capacity must be at least 1; a new departure defaults to status `AVAILABLE`; adding a departure to an unknown package returns `404`.

### User Story C4: Check Departure Availability
- **As a** Customer / Administrator, **I want to** check how many places are still available on a departure.
- **Rule**: `availableCapacity = capacity − Σ quantity of reservations with status RESERVED or CONFIRMED`. Released reservations do not consume capacity.

### User Story C5: Reserve, Confirm and Release Inventory
- **As the** System, **I want to** reserve `quantity` places on a departure for a booking when a `BookingCreated` event is received.
- **As an** Administrator, **I want to** create, view, confirm and release reservations directly via REST for support and testing purposes.
- **Scenario (reserve)**:
  1. Look up the departure; if it does not exist → outcome `DEPARTURE_NOT_FOUND`.
  2. If the departure status is not `AVAILABLE` → outcome `DEPARTURE_UNAVAILABLE`.
  3. If a reservation already exists for the same `bookingId` → outcome `DUPLICATE_BOOKING`.
  4. If `quantity` exceeds the available capacity → outcome `INSUFFICIENT_CAPACITY`.
  5. Otherwise create the reservation with status `RESERVED` → outcome `CREATED`.
- **Event outcome**: `CREATED` publishes `InventoryReserved`; any other outcome publishes `InventoryRejected` with the outcome name as the reason.
- **Confirm / Release**: a `BookingConfirmed` event moves the reservation to `CONFIRMED`; a `BookingFailed` event moves it to `RELEASED`, restoring capacity.

---

## Booking Service

### User Story B1: Create a Booking
- **As a** Customer, **I want to** create a booking for a package departure by providing my customer ID, the package ID, the departure ID, the number of places and the total price.
- **Scenario**:
  1. The customer submits the booking request.
  2. The Booking Service validates the request (`customerId`, `packageId`, `departureId` required; `quantity ≥ 1`).
  3. The booking is saved with status `PENDING_INVENTORY` and returned to the customer (`201 Created`).
  4. **Inter-Service Event**: the Booking Service publishes `BookingCreated` to Kafka so the Catalogue Service can reserve inventory.

### User Story B2: Get/list Bookings
- **As a** Customer / Administrator, **I want to** retrieve a list of all bookings.
- **As a** Customer / Administrator, **I want to** get a booking by its ID, including its current status, reservation ID and payment ID.

### User Story B3: Delete a Booking
- **As a** Customer / Administrator, **I want to** delete a booking record by its ID.
- **Note**: deleting a booking does not currently release its inventory reservation (see *Future Considerations* in the technical architecture).

### User Story B4: Booking Lifecycle driven by Events
- **As the** System, **I want** the booking status to advance automatically in response to Kafka events, without a public endpoint for changing status.
- **Scenario (happy path)**:
  1. `InventoryReserved` received while `PENDING_INVENTORY` → store `reservationId`, set `PENDING_PAYMENT`, publish `PaymentRequested`.
  2. `PaymentCompleted` received while `PENDING_PAYMENT` → store `paymentId`, set `CONFIRMED`, publish `BookingConfirmed`.
- **Scenario (failure paths)**:
  - `InventoryRejected` received while `PENDING_INVENTORY` → set `FAILED` (no reservation to release).
  - `PaymentFailed` received while `PENDING_PAYMENT` → store `paymentId`, set `FAILED`, publish `BookingFailed` (Catalogue releases the reservation).
- **Rule**: events that arrive when the booking is not in the expected state are ignored, protecting against duplicate or out-of-order messages.

```text
PENDING_INVENTORY ──InventoryReserved──▶ PENDING_PAYMENT ──PaymentCompleted──▶ CONFIRMED
        │                                       │
   InventoryRejected                       PaymentFailed
        ▼                                       ▼
      FAILED                                  FAILED
```

---

## Payment Service

### User Story P1: Process Payment for a Booking
- **As the** System, **I want to** process a payment when a `PaymentRequested` event is received for a booking.
- **Scenario**:
  1. If a payment already exists for the `bookingId`, return it and do nothing else (idempotent).
  2. Otherwise create a `Payment` with status `PENDING` for the booking, customer and amount.
  3. Apply the prototype gateway rule: `amount > 0` → `COMPLETED` and publish `PaymentCompleted`; `amount ≤ 0` → `FAILED` and publish `PaymentFailed` with reason *"Payment amount must be greater than zero"*.
  4. Record `processedAt`.

### User Story P2: View Payments
- **As a** Customer / Administrator, **I want to** retrieve a list of all payments.
- **As a** Customer / Administrator, **I want to** get a payment by its payment ID or by the booking ID it belongs to.

---

## Notification Service

### User Story N1: Notify Customer of Booking Outcome
- **As the** System, **I want to** create a notification for the customer when a booking is confirmed or fails.
- **Scenario**:
  - `BookingConfirmed` → notification of type `BOOKING_CONFIRMED` with message *"Your booking has been confirmed."*
  - `BookingFailed` → notification of type `BOOKING_FAILED` with message *"Your booking could not be completed: {reason}"*
  - Notifications are stored with status `SENT` (delivery channel is simulated).

### User Story N2: View Notifications
- **As a** Customer / Administrator, **I want to** retrieve a list of all notifications.
- **As a** Customer / Administrator, **I want to** get a notification by its ID.
- **As a** Customer, **I want to** list all notifications for my customer ID.
- **As a** Customer / Administrator, **I want to** list all notifications related to a booking ID.
