
package com.travelbooking.stream_analytics_service.analytics;


import org.springframework.beans.factory.ObjectProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnalyticsServiceTest {

    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        analyticsService = new AnalyticsService(null);
    }

    @Test
    void shouldStartWithZeroAnalytics() {
        assertEquals(0, analyticsService.getTotalBookings());
        assertEquals(0, analyticsService.getConfirmedBookings());
        assertEquals(0, analyticsService.getFailedBookings());
        assertEquals(0.0, analyticsService.getSuccessRate());
    }

    @Test
    void shouldSetTotalBookings() {
        analyticsService.setTotalBookings(2);

        assertEquals(2, analyticsService.getTotalBookings());
    }

    @Test
    void shouldSetConfirmedBookings() {
        analyticsService.setTotalBookings(1);
        analyticsService.setConfirmedBookings(1);

        assertEquals(1, analyticsService.getTotalBookings());
        assertEquals(1, analyticsService.getConfirmedBookings());
        assertEquals(100.0, analyticsService.getSuccessRate());
    }

    @Test
    void shouldSetFailedBookings() {
        analyticsService.setTotalBookings(1);
        analyticsService.setFailedBookings(1);

        assertEquals(1, analyticsService.getTotalBookings());
        assertEquals(1, analyticsService.getFailedBookings());
        assertEquals(0.0, analyticsService.getSuccessRate());
    }

    @Test
    void shouldCalculateSuccessRate() {
        analyticsService.setTotalBookings(2);
        analyticsService.setConfirmedBookings(1);
        analyticsService.setFailedBookings(1);

        assertEquals(2, analyticsService.getTotalBookings());
        assertEquals(1, analyticsService.getConfirmedBookings());
        assertEquals(1, analyticsService.getFailedBookings());
        assertEquals(50.0, analyticsService.getSuccessRate());
    }

    @Test
    void shouldReplaceExistingCounts() {
        analyticsService.setTotalBookings(5);
        analyticsService.setTotalBookings(3);

        assertEquals(3, analyticsService.getTotalBookings());
    }
}
