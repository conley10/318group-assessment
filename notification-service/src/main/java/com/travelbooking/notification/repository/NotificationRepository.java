package com.travelbooking.notification.repository;

import com.travelbooking.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository
        extends JpaRepository<Notification, UUID> {

    List<Notification> findByCustomerId(UUID customerId);

    List<Notification> findByBookingId(UUID bookingId);
}