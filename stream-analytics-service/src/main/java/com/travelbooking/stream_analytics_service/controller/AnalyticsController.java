package com.travelbooking.stream_analytics_service.controller;

import com.travelbooking.stream_analytics_service.analytics.AnalyticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping
    public Map<String, Object> getAnalytics() {

        Map<String, Object> analytics = new LinkedHashMap<>();

        analytics.put("totalBookings", analyticsService.getTotalBookings());
        analytics.put("confirmedBookings", analyticsService.getConfirmedBookings());
        analytics.put("failedBookings", analyticsService.getFailedBookings());
        analytics.put("successRate", analyticsService.getSuccessRate());

        return analytics;
    }
}