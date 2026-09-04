package com.travelbooking.stream_analytics_service.stream;

import com.travelbooking.stream_analytics_service.analytics.AnalyticsService;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class BookingAnalyticsStreams {

    private final AnalyticsService analyticsService;

    public BookingAnalyticsStreams(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @Bean
    public Consumer<KStream<String, String>> bookingCreatedStream() {
        return stream -> stream.foreach((key, value) -> {
            analyticsService.bookingCreated();
            System.out.println("Analytics received booking-created: " + value);
        });
    }

    @Bean
    public Consumer<KStream<String, String>> bookingConfirmedStream() {
        return stream -> stream.foreach((key, value) -> {
            analyticsService.bookingConfirmed();
            System.out.println("Analytics received booking-confirmed: " + value);
        });
    }

    @Bean
    public Consumer<KStream<String, String>> bookingFailedStream() {
        return stream -> stream.foreach((key, value) -> {
            analyticsService.bookingFailed();
            System.out.println("Analytics received booking-failed: " + value);
        });
    }
}