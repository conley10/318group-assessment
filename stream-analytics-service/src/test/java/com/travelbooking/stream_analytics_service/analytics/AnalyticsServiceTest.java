package com.travelbooking.stream_analytics_service.analytics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnalyticsServiceTest {

    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        analyticsService = new AnalyticsService();
    }

    @Test
    void shouldStartWithZeroAnalytics() {
        assertEquals(0, analyticsService.getTotalBookings());
        assertEquals(0, analyticsService.getConfirmedBookings());
        assertEquals(0, analyticsService.getFailedBookings());
        assertEquals(0.0, analyticsService.getSuccessRate());
    }

    @Test
    void shouldCountCreatedBookings() {
        analyticsService.bookingCreated();
        analyticsService.bookingCreated();

        assertEquals(2, analyticsService.getTotalBookings());
    }

    @Test
    void shouldCountConfirmedBookings() {
        analyticsService.bookingCreated();
        analyticsService.bookingConfirmed();

        assertEquals(1, analyticsService.getTotalBookings());
        assertEquals(1, analyticsService.getConfirmedBookings());
        assertEquals(100.0, analyticsService.getSuccessRate());
    }

    @Test
    void shouldCountFailedBookings() {
        analyticsService.bookingCreated();
        analyticsService.bookingFailed();

        assertEquals(1, analyticsService.getTotalBookings());
        assertEquals(1, analyticsService.getFailedBookings());
        assertEquals(0.0, analyticsService.getSuccessRate());
    }

    @Test
    void shouldCalculateSuccessRate() {
        analyticsService.bookingCreated();
        analyticsService.bookingCreated();

        analyticsService.bookingConfirmed();
        analyticsService.bookingFailed();

        assertEquals(2, analyticsService.getTotalBookings());
        assertEquals(1, analyticsService.getConfirmedBookings());
        assertEquals(1, analyticsService.getFailedBookings());
        assertEquals(50.0, analyticsService.getSuccessRate());
    }
}