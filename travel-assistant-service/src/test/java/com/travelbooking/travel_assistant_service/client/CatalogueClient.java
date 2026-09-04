package com.travelassistant.travel_assistant_service.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CatalogueClient {

    private final RestClient restClient;

    public CatalogueClient() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:8081")
                .build();
    }

    public String getAllPackages() {
        return restClient.get()
                .uri("/api/packages")
                .retrieve()
                .body(String.class);
    }
}