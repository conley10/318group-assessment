package com.travelbooking.booking.service;

import com.travelbooking.booking.model.Booking;
import com.travelbooking.booking.model.BookingStatus;
import com.travelbooking.booking.repository.BookingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository repository;

    public BookingService(BookingRepository repository) {
        this.repository = repository;
    }

    public List<Booking> getAllBookings() {
        return repository.findAll();
    }

    public Optional<Booking> getBookingById(UUID bookingId) {
        return repository.findById(bookingId);
    }

    public Booking createBooking(Booking booking) {
        booking.setBookingId(null);
        booking.setReservationId(null);
        booking.setPaymentId(null);
        booking.setStatus(BookingStatus.PENDING_INVENTORY);

        return repository.save(booking);
    }

    public Optional<Booking> updateStatus(
            UUID bookingId,
            BookingStatus status
    ) {
        return repository.findById(bookingId)
                .map(booking -> {
                    booking.setStatus(status);
                    return repository.save(booking);
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