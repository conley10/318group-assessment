package com.travelbooking.booking.service;

import com.travelbooking.booking.event.BookingCreatedEvent;
import com.travelbooking.booking.event.InventoryRejectedEvent;
import com.travelbooking.booking.event.InventoryReservedEvent;
import com.travelbooking.booking.event.PaymentRequestedEvent;
import com.travelbooking.booking.model.Booking;
import com.travelbooking.booking.model.BookingStatus;
import com.travelbooking.booking.repository.BookingRepository;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.travelbooking.booking.event.BookingConfirmedEvent;
import com.travelbooking.booking.event.BookingFailedEvent;
import com.travelbooking.booking.event.PaymentCompletedEvent;
import com.travelbooking.booking.event.PaymentFailedEvent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository repository;
    private final StreamBridge streamBridge;

    public BookingService(
            BookingRepository repository,
            StreamBridge streamBridge
    ) {
        this.repository = repository;
        this.streamBridge = streamBridge;
    }

    public List<Booking> getAllBookings() {
        return repository.findAll();
    }

    public Optional<Booking> getBookingById(UUID bookingId) {
        return repository.findById(bookingId);
    }

    @Transactional
    public Booking createBooking(Booking booking) {
        booking.setBookingId(null);
        booking.setReservationId(null);
        booking.setPaymentId(null);
        booking.setStatus(BookingStatus.PENDING_INVENTORY);

        Booking saved = repository.save(booking);

        BookingCreatedEvent event = new BookingCreatedEvent(
                saved.getBookingId(),
                saved.getCustomerId(),
                saved.getPackageId(),
                saved.getDepartureId(),
                saved.getQuantity(),
                saved.getTotalPrice()
        );

        streamBridge.send(
                "bookingCreated-out-0",
                event
        );

        return saved;
    }

    @Transactional
    public Optional<Booking> handleInventoryReserved(
            InventoryReservedEvent event
    ) {
        return repository.findById(event.bookingId())
                .map(booking -> {

                    if (booking.getStatus()
                            != BookingStatus.PENDING_INVENTORY) {
                        return booking;
                    }

                    booking.setReservationId(
                            event.reservationId()
                    );

                    booking.setStatus(
                            BookingStatus.PENDING_PAYMENT
                    );

                    Booking saved = repository.save(booking);

                    PaymentRequestedEvent paymentEvent =
                            new PaymentRequestedEvent(
                                    saved.getBookingId(),
                                    saved.getCustomerId(),
                                    saved.getTotalPrice()
                            );

                    streamBridge.send(
                            "paymentRequested-out-0",
                            paymentEvent
                    );

                    return saved;
                });
    }

    @Transactional
    public Optional<Booking> handleInventoryRejected(
            InventoryRejectedEvent event
    ) {
        return repository.findById(event.bookingId())
                .map(booking -> {

                    if (booking.getStatus()
                            != BookingStatus.PENDING_INVENTORY) {
                        return booking;
                    }

                    booking.setStatus(
                            BookingStatus.FAILED
                    );

                    return repository.save(booking);
                });
    }

    @Transactional
public Optional<Booking> handlePaymentCompleted(
        PaymentCompletedEvent event
) {
    return repository.findById(event.bookingId())
            .map(booking -> {

                if (booking.getStatus()
                        != BookingStatus.PENDING_PAYMENT) {
                    return booking;
                }

                booking.setPaymentId(event.paymentId());
                booking.setStatus(BookingStatus.CONFIRMED);

                Booking saved = repository.save(booking);

BookingConfirmedEvent confirmedEvent =
        new BookingConfirmedEvent(
                saved.getBookingId(),
                saved.getCustomerId(),
                saved.getReservationId()
        );

                streamBridge.send(
                        "bookingConfirmed-out-0",
                        confirmedEvent
                );

                return saved;
            });
}

@Transactional
public Optional<Booking> handlePaymentFailed(
        PaymentFailedEvent event
) {
    return repository.findById(event.bookingId())
            .map(booking -> {

                if (booking.getStatus()
                        != BookingStatus.PENDING_PAYMENT) {
                    return booking;
                }

                booking.setPaymentId(event.paymentId());
                booking.setStatus(BookingStatus.FAILED);

                Booking saved = repository.save(booking);

BookingFailedEvent failedEvent =
        new BookingFailedEvent(
                saved.getBookingId(),
                saved.getCustomerId(),
                saved.getReservationId(),
                event.reason()
        );

                streamBridge.send(
                        "bookingFailed-out-0",
                        failedEvent
                );

                return saved;
            });
}


    public boolean deleteBooking(UUID bookingId) {
        if (!repository.existsById(bookingId)) {
            return false;
        }

        repository.deleteById(bookingId);
        return true;
    }
}