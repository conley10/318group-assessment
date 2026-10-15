
package com.travelbooking.stream_analytics_service.controller;

import com.travelbooking.stream_analytics_service.analytics.AnalyticsService;
import org.junit.jupiter.api.Test;

import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnalyticsControllerTest {

    @Test
    void shouldReturnCurrentAnalytics() {

        AnalyticsService analyticsService = new AnalyticsService(null);

        analyticsService.setTotalBookings(2);
        analyticsService.setConfirmedBookings(1);
        analyticsService.setFailedBookings(1);

        AnalyticsController controller =
                new AnalyticsController(analyticsService);

        Map<String, Object> result = controller.getAnalytics();

        assertEquals(2L, result.get("totalBookings"));
        assertEquals(1L, result.get("confirmedBookings"));
        assertEquals(1L, result.get("failedBookings"));
        assertEquals(50.0, result.get("successRate"));
    }
}
