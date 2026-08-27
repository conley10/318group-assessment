package com.travelbooking.notification.service;

import com.travelbooking.notification.event.BookingConfirmedEvent;
import com.travelbooking.notification.event.BookingFailedEvent;
import com.travelbooking.notification.model.Notification;
import com.travelbooking.notification.model.NotificationStatus;
import com.travelbooking.notification.model.NotificationType;
import com.travelbooking.notification.repository.NotificationRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository repository;

    public NotificationService(
            NotificationRepository repository
    ) {
        this.repository = repository;
    }

    public List<Notification> getAllNotifications() {
        return repository.findAll();
    }

    public Optional<Notification> getNotificationById(
            UUID notificationId
    ) {
        return repository.findById(notificationId);
    }

    public List<Notification> getNotificationsByCustomer(
            UUID customerId
    ) {
        return repository.findByCustomerId(customerId);
    }

    public List<Notification> getNotificationsByBooking(
            UUID bookingId
    ) {
        return repository.findByBookingId(bookingId);
    }

    public Notification createBookingConfirmedNotification(
            BookingConfirmedEvent event
    ) {
        Notification notification = new Notification();

        notification.setBookingId(event.bookingId());
        notification.setCustomerId(event.customerId());
        notification.setType(
                NotificationType.BOOKING_CONFIRMED
        );
        notification.setMessage(
                "Your booking has been confirmed."
        );
        notification.setStatus(
                NotificationStatus.SENT
        );

        return repository.save(notification);
    }

    public Notification createBookingFailedNotification(
            BookingFailedEvent event
    ) {
        Notification notification = new Notification();

        notification.setBookingId(event.bookingId());
        notification.setCustomerId(event.customerId());
        notification.setType(
                NotificationType.BOOKING_FAILED
        );

        notification.setMessage(
                "Your booking could not be completed: "
                        + event.reason()
        );

        notification.setStatus(
                NotificationStatus.SENT
        );

        return repository.save(notification);
    }
}