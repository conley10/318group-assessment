package com.travelbooking.payment.service;

import com.travelbooking.payment.event.PaymentCompletedEvent;
import com.travelbooking.payment.event.PaymentFailedEvent;
import com.travelbooking.payment.event.PaymentRequestedEvent;
import com.travelbooking.payment.model.Payment;
import com.travelbooking.payment.model.PaymentStatus;
import com.travelbooking.payment.repository.PaymentRepository;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository repository;
    private final StreamBridge streamBridge;

    public PaymentService(
            PaymentRepository repository,
            StreamBridge streamBridge
    ) {
        this.repository = repository;
        this.streamBridge = streamBridge;
    }

    public List<Payment> getAllPayments() {
        return repository.findAll();
    }

    public Optional<Payment> getPaymentById(UUID paymentId) {
        return repository.findById(paymentId);
    }

    public Optional<Payment> getPaymentByBookingId(UUID bookingId) {
        return repository.findByBookingId(bookingId);
    }

    @Transactional
    public Payment processPayment(
            PaymentRequestedEvent event
    ) {
        Optional<Payment> existing =
                repository.findByBookingId(event.bookingId());

        if (existing.isPresent()) {
            return existing.get();
        }

        Payment payment = new Payment();

        payment.setBookingId(event.bookingId());
        payment.setCustomerId(event.customerId());
        payment.setAmount(event.amount());
        payment.setStatus(PaymentStatus.PENDING);

        Payment saved = repository.save(payment);

        /*
         * Prototype payment rule:
         *
         * amount > 0  -> payment succeeds
         * amount <= 0 -> payment fails
         *
         * This simulates an external payment gateway.
         */

        if (saved.getAmount() > 0) {

            saved.setStatus(PaymentStatus.COMPLETED);
            saved.setProcessedAt(LocalDateTime.now());

            saved = repository.save(saved);

            PaymentCompletedEvent completedEvent =
                    new PaymentCompletedEvent(
                            saved.getBookingId(),
                            saved.getPaymentId()
                    );

            streamBridge.send(
                    "paymentCompleted-out-0",
                    completedEvent
            );

        } else {

            saved.setStatus(PaymentStatus.FAILED);
            saved.setProcessedAt(LocalDateTime.now());

            saved = repository.save(saved);

            PaymentFailedEvent failedEvent =
                    new PaymentFailedEvent(
                            saved.getBookingId(),
                            saved.getPaymentId(),
                            "Payment amount must be greater than zero"
                    );

            streamBridge.send(
                    "paymentFailed-out-0",
                    failedEvent
            );
        }

        return saved;
    }
}