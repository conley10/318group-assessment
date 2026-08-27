package com.travelbooking.payment.event;

import java.util.UUID;

public record PaymentFailedEvent(
        UUID bookingId,
        UUID paymentId,
        String reason
) {
}