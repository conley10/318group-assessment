package com.travelbooking.stream_analytics_service.stream;

import com.travelbooking.stream_analytics_service.analytics.AnalyticsService;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
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
        return stream -> stream
                .map((key, value) ->
                        KeyValue.pair("total", 1L))
                .groupByKey(Grouped.with(Serdes.String(), Serdes.Long()))
                .count(Materialized.as("booking-created-count-store"))
                .toStream()
                .foreach((key, count) ->
                        analyticsService.setTotalBookings(count));
    }

    @Bean
    public Consumer<KStream<String, String>> bookingConfirmedStream() {
        return stream -> stream
                .map((key, value) ->
                        KeyValue.pair("confirmed", 1L))
                .groupByKey(Grouped.with(Serdes.String(), Serdes.Long()))
                .count(Materialized.as("booking-confirmed-count-store"))
                .toStream()
                .foreach((key, count) ->
                        analyticsService.setConfirmedBookings(count));
    }

    @Bean
    public Consumer<KStream<String, String>> bookingFailedStream() {
        return stream -> stream
                .map((key, value) ->
                        KeyValue.pair("failed", 1L))
                .groupByKey(Grouped.with(Serdes.String(), Serdes.Long()))
                .count(Materialized.as("booking-failed-count-store"))
                .toStream()
                .foreach((key, count) ->
                        analyticsService.setFailedBookings(count));
    }
}