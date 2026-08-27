package com.travelbooking.notification.controller;

import com.travelbooking.notification.model.Notification;
import com.travelbooking.notification.service.NotificationService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(
            NotificationService service
    ) {
        this.service = service;
    }

    @GetMapping
    public List<Notification> getAllNotifications() {
        return service.getAllNotifications();
    }

    @GetMapping("/{notificationId}")
    public ResponseEntity<Notification> getNotificationById(
            @PathVariable UUID notificationId
    ) {
        return service.getNotificationById(notificationId)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    @GetMapping("/customer/{customerId}")
    public List<Notification> getNotificationsByCustomer(
            @PathVariable UUID customerId
    ) {
        return service.getNotificationsByCustomer(customerId);
    }

    @GetMapping("/booking/{bookingId}")
    public List<Notification> getNotificationsByBooking(
            @PathVariable UUID bookingId
    ) {
        return service.getNotificationsByBooking(bookingId);
    }
}