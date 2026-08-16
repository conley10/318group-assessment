package com.travelbooking.booking.repository;

import com.travelbooking.booking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BookingRepository
        extends JpaRepository<Booking, UUID> {
}