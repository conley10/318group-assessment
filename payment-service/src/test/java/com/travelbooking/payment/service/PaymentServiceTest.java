package com.travelbooking.payment.service;

import com.travelbooking.payment.event.PaymentCompletedEvent;
import com.travelbooking.payment.event.PaymentFailedEvent;
import com.travelbooking.payment.event.PaymentRequestedEvent;
import com.travelbooking.payment.model.Payment;
import com.travelbooking.payment.model.PaymentStatus;
import com.travelbooking.payment.repository.PaymentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.cloud.stream.function.StreamBridge;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository repository;

    @Mock
    private StreamBridge streamBridge;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService =
                new PaymentService(repository, streamBridge);
    }

    @Test
    void shouldGetAllPayments() {

        Payment payment = createPayment();

        when(repository.findAll())
                .thenReturn(List.of(payment));

        List<Payment> result =
                paymentService.getAllPayments();

        assertEquals(1, result.size());
        assertEquals(
                payment.getPaymentId(),
                result.get(0).getPaymentId()
        );

        verify(repository).findAll();
    }

    @Test
    void shouldGetPaymentById() {

        Payment payment = createPayment();

        when(repository.findById(payment.getPaymentId()))
                .thenReturn(Optional.of(payment));

        Optional<Payment> result =
                paymentService.getPaymentById(
                        payment.getPaymentId()
                );

        assertTrue(result.isPresent());
        assertEquals(
                payment.getPaymentId(),
                result.get().getPaymentId()
        );
    }

    @Test
    void shouldGetPaymentByBookingId() {

        Payment payment = createPayment();

        when(repository.findByBookingId(
                payment.getBookingId()
        )).thenReturn(Optional.of(payment));

        Optional<Payment> result =
                paymentService.getPaymentByBookingId(
                        payment.getBookingId()
                );

        assertTrue(result.isPresent());
        assertEquals(
                payment.getBookingId(),
                result.get().getBookingId()
        );
    }

    @Test
    void shouldCompletePaymentWhenAmountIsPositive() {

        UUID bookingId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        PaymentRequestedEvent event =
                new PaymentRequestedEvent(
                        bookingId,
                        customerId,
                        4399.98
                );

        when(repository.findByBookingId(bookingId))
                .thenReturn(Optional.empty());

        when(repository.save(any(Payment.class)))
                .thenAnswer(invocation -> {

                    Payment payment =
                            invocation.getArgument(0);

                    if (payment.getPaymentId() == null) {
                        payment.setPaymentId(paymentId);
                    }

                    return payment;
                });

        when(streamBridge.send(
                eq("paymentCompleted-out-0"),
                any(PaymentCompletedEvent.class)
        )).thenReturn(true);

        Payment result =
                paymentService.processPayment(event);

        assertEquals(
                PaymentStatus.COMPLETED,
                result.getStatus()
        );

        assertEquals(bookingId, result.getBookingId());
        assertEquals(customerId, result.getCustomerId());
        assertEquals(4399.98, result.getAmount(), 0.001);
        assertNotNull(result.getProcessedAt());

        ArgumentCaptor<PaymentCompletedEvent> captor =
                ArgumentCaptor.forClass(
                        PaymentCompletedEvent.class
                );

        verify(streamBridge).send(
                eq("paymentCompleted-out-0"),
                captor.capture()
        );

        PaymentCompletedEvent published =
                captor.getValue();

        assertEquals(
                bookingId,
                published.bookingId()
        );

        assertEquals(
                paymentId,
                published.paymentId()
        );

        verify(
                repository,
                times(2)
        ).save(any(Payment.class));

        verify(
                streamBridge,
                never()
        ).send(
                eq("paymentFailed-out-0"),
                any(PaymentFailedEvent.class)
        );
    }

    @Test
    void shouldFailPaymentWhenAmountIsZero() {

        UUID bookingId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        PaymentRequestedEvent event =
                new PaymentRequestedEvent(
                        bookingId,
                        customerId,
                        0.0
                );

        when(repository.findByBookingId(bookingId))
                .thenReturn(Optional.empty());

        when(repository.save(any(Payment.class)))
                .thenAnswer(invocation -> {

                    Payment payment =
                            invocation.getArgument(0);

                    if (payment.getPaymentId() == null) {
                        payment.setPaymentId(paymentId);
                    }

                    return payment;
                });

        when(streamBridge.send(
                eq("paymentFailed-out-0"),
                any(PaymentFailedEvent.class)
        )).thenReturn(true);

        Payment result =
                paymentService.processPayment(event);

        assertEquals(
                PaymentStatus.FAILED,
                result.getStatus()
        );

        assertEquals(bookingId, result.getBookingId());
        assertEquals(0.0, result.getAmount(), 0.001);
        assertNotNull(result.getProcessedAt());

        ArgumentCaptor<PaymentFailedEvent> captor =
                ArgumentCaptor.forClass(
                        PaymentFailedEvent.class
                );

        verify(streamBridge).send(
                eq("paymentFailed-out-0"),
                captor.capture()
        );

        PaymentFailedEvent published =
                captor.getValue();

        assertEquals(
                bookingId,
                published.bookingId()
        );

        assertEquals(
                paymentId,
                published.paymentId()
        );

        assertEquals(
                "Payment amount must be greater than zero",
                published.reason()
        );

        verify(
                streamBridge,
                never()
        ).send(
                eq("paymentCompleted-out-0"),
                any(PaymentCompletedEvent.class)
        );
    }

    @Test
    void shouldFailPaymentWhenAmountIsNegative() {

        UUID bookingId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        PaymentRequestedEvent event =
                new PaymentRequestedEvent(
                        bookingId,
                        customerId,
                        -100.00
                );

        when(repository.findByBookingId(bookingId))
                .thenReturn(Optional.empty());

        when(repository.save(any(Payment.class)))
                .thenAnswer(invocation -> {

                    Payment payment =
                            invocation.getArgument(0);

                    if (payment.getPaymentId() == null) {
                        payment.setPaymentId(
                                UUID.randomUUID()
                        );
                    }

                    return payment;
                });

        Payment result =
                paymentService.processPayment(event);

        assertEquals(
                PaymentStatus.FAILED,
                result.getStatus()
        );

        verify(streamBridge).send(
                eq("paymentFailed-out-0"),
                any(PaymentFailedEvent.class)
        );
    }

    @Test
    void shouldNotCreateDuplicatePaymentForSameBooking() {

        Payment existing = createPayment();

        existing.setStatus(PaymentStatus.COMPLETED);

        PaymentRequestedEvent event =
                new PaymentRequestedEvent(
                        existing.getBookingId(),
                        existing.getCustomerId(),
                        existing.getAmount()
                );

        when(repository.findByBookingId(
                existing.getBookingId()
        )).thenReturn(Optional.of(existing));

        Payment result =
                paymentService.processPayment(event);

        assertSame(existing, result);

        verify(
                repository,
                never()
        ).save(any(Payment.class));

        verifyNoInteractions(streamBridge);
    }

    private Payment createPayment() {

        Payment payment = new Payment();

        payment.setPaymentId(UUID.randomUUID());
        payment.setBookingId(UUID.randomUUID());
        payment.setCustomerId(UUID.randomUUID());
        payment.setAmount(4399.98);
        payment.setStatus(PaymentStatus.COMPLETED);

        return payment;
    }
}