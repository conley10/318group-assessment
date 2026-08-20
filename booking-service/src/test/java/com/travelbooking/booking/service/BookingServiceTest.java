package com.travelbooking.booking.service;

import com.travelbooking.booking.event.InventoryRejectedEvent;
import com.travelbooking.booking.event.InventoryReservedEvent;
import com.travelbooking.booking.event.PaymentCompletedEvent;
import com.travelbooking.booking.event.PaymentFailedEvent;
import com.travelbooking.booking.model.Booking;
import com.travelbooking.booking.model.BookingStatus;
import com.travelbooking.booking.repository.BookingRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.cloud.stream.function.StreamBridge;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository repository;

    @Mock
    private StreamBridge streamBridge;

    private BookingService service;

    private Booking booking;

    private UUID bookingId;
    private UUID customerId;
    private UUID reservationId;

    @BeforeEach
    void setUp() {

        service = new BookingService(
                repository,
                streamBridge
        );

        bookingId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        reservationId = UUID.randomUUID();

        booking = new Booking();

        booking.setBookingId(bookingId);
        booking.setCustomerId(customerId);
        booking.setPackageId(1L);
        booking.setDepartureId(1L);
        booking.setQuantity(2);
        booking.setTotalPrice(4399.98);
    }

    @Test
    void shouldCreateBookingAsPendingInventory() {

        when(repository.save(any(Booking.class)))
                .thenAnswer(invocation -> {

                    Booking saved =
                            invocation.getArgument(0);

                    saved.setBookingId(bookingId);

                    return saved;
                });

        Booking result =
                service.createBooking(booking);

        assertEquals(
                BookingStatus.PENDING_INVENTORY,
                result.getStatus()
        );

        assertNull(result.getReservationId());
        assertNull(result.getPaymentId());

        verify(repository, times(1))
                .save(booking);

        verify(streamBridge, times(1))
                .send(
                        eq("bookingCreated-out-0"),
                        any()
                );
    }

    @Test
    void shouldMoveToPendingPaymentWhenInventoryReserved() {

        booking.setStatus(
                BookingStatus.PENDING_INVENTORY
        );

        when(repository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(repository.save(any(Booking.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        InventoryReservedEvent event =
                new InventoryReservedEvent(
                        bookingId,
                        reservationId,
                        1L,
                        2
                );

        Optional<Booking> result =
                service.handleInventoryReserved(event);

        assertTrue(result.isPresent());

        assertEquals(
                BookingStatus.PENDING_PAYMENT,
                result.get().getStatus()
        );

        assertEquals(
                reservationId,
                result.get().getReservationId()
        );

        verify(streamBridge, times(1))
                .send(
                        eq("paymentRequested-out-0"),
                        any()
                );
    }

    @Test
    void shouldFailBookingWhenInventoryRejected() {

        booking.setStatus(
                BookingStatus.PENDING_INVENTORY
        );

        when(repository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(repository.save(any(Booking.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        InventoryRejectedEvent event =
                new InventoryRejectedEvent(
                        bookingId,
                        1L,
                        "INSUFFICIENT_CAPACITY"
                );

        Optional<Booking> result =
                service.handleInventoryRejected(event);

        assertTrue(result.isPresent());

        assertEquals(
                BookingStatus.FAILED,
                result.get().getStatus()
        );

        verify(repository, times(1))
                .save(booking);
    }

    @Test
    void shouldConfirmBookingWhenPaymentCompleted() {

        booking.setStatus(
                BookingStatus.PENDING_PAYMENT
        );

        booking.setReservationId(reservationId);

        UUID paymentId = UUID.randomUUID();

        when(repository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(repository.save(any(Booking.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        PaymentCompletedEvent event =
                new PaymentCompletedEvent(
                        bookingId,
                        paymentId
                );

        Optional<Booking> result =
                service.handlePaymentCompleted(event);

        assertTrue(result.isPresent());

        assertEquals(
                BookingStatus.CONFIRMED,
                result.get().getStatus()
        );

        assertEquals(
                paymentId,
                result.get().getPaymentId()
        );

        verify(streamBridge, times(1))
                .send(
                        eq("bookingConfirmed-out-0"),
                        any()
                );
    }

    @Test
    void shouldFailBookingWhenPaymentFails() {

        booking.setStatus(
                BookingStatus.PENDING_PAYMENT
        );

        booking.setReservationId(reservationId);

        UUID paymentId = UUID.randomUUID();

        when(repository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(repository.save(any(Booking.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        PaymentFailedEvent event =
                new PaymentFailedEvent(
                        bookingId,
                        paymentId,
                        "Payment declined"
                );

        Optional<Booking> result =
                service.handlePaymentFailed(event);

        assertTrue(result.isPresent());

        assertEquals(
                BookingStatus.FAILED,
                result.get().getStatus()
        );

        assertEquals(
                paymentId,
                result.get().getPaymentId()
        );

        verify(streamBridge, times(1))
                .send(
                        eq("bookingFailed-out-0"),
                        any()
                );
    }

    @Test
    void shouldIgnoreInventoryReservedWhenBookingIsNotPendingInventory() {

        booking.setStatus(
                BookingStatus.CONFIRMED
        );

        when(repository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        InventoryReservedEvent event =
                new InventoryReservedEvent(
                        bookingId,
                        reservationId,
                        1L,
                        2
                );

        Optional<Booking> result =
                service.handleInventoryReserved(event);

        assertTrue(result.isPresent());

        assertEquals(
                BookingStatus.CONFIRMED,
                result.get().getStatus()
        );

        verify(repository, never())
                .save(any());

        verify(streamBridge, never())
                .send(
                        eq("paymentRequested-out-0"),
                        any()
                );
    }

    @Test
    void shouldIgnorePaymentCompletedWhenNotPendingPayment() {

        booking.setStatus(
                BookingStatus.PENDING_INVENTORY
        );

        UUID paymentId = UUID.randomUUID();

        when(repository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        PaymentCompletedEvent event =
                new PaymentCompletedEvent(
                        bookingId,
                        paymentId
                );

        Optional<Booking> result =
                service.handlePaymentCompleted(event);

        assertTrue(result.isPresent());

        assertEquals(
                BookingStatus.PENDING_INVENTORY,
                result.get().getStatus()
        );

        assertNull(result.get().getPaymentId());

        verify(repository, never())
                .save(any());

        verify(streamBridge, never())
                .send(
                        eq("bookingConfirmed-out-0"),
                        any()
                );
    }
}