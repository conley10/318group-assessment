package com.travelbooking.booking.event;

import java.util.UUID;

public record PaymentFailedEvent(
        UUID bookingId,
        UUID paymentId,
        String reason
) {
}