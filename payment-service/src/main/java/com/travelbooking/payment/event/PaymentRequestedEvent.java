package com.travelbooking.payment.event;

import java.util.UUID;

public record PaymentRequestedEvent(
        UUID bookingId,
        UUID customerId,
        double amount
) {
}