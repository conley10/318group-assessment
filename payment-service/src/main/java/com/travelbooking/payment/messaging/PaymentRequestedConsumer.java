package com.travelbooking.payment.messaging;

import com.travelbooking.payment.event.PaymentRequestedEvent;
import com.travelbooking.payment.service.PaymentService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class PaymentRequestedConsumer {

    private final PaymentService paymentService;

    public PaymentRequestedConsumer(
            PaymentService paymentService
    ) {
        this.paymentService = paymentService;
    }

    @Bean
    public Consumer<PaymentRequestedEvent> paymentRequested() {
        return event -> {

            System.out.println(
                    "Payment Service received PaymentRequested: "
                            + event.bookingId()
            );

            paymentService.processPayment(event);
        };
    }
}