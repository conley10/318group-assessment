package com.travelassistant.travel_assistant_service.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CatalogueClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public CatalogueClient() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:8081")
                .build();

        this.objectMapper = new ObjectMapper();
    }

    public String getAllPackages() {
        try {
            String packagesJson = restClient.get()
                    .uri("/api/packages")
                    .retrieve()
                    .body(String.class);

            JsonNode packages =
                    objectMapper.readTree(packagesJson);

            StringBuilder catalogue =
                    new StringBuilder();

            for (JsonNode travelPackage : packages) {

                long packageId =
                        travelPackage.get("packageId").asLong();

                String departuresJson = restClient.get()
                        .uri("/api/packages/"
                                + packageId
                                + "/departures")
                        .retrieve()
                        .body(String.class);

                catalogue.append("PACKAGE:\n");
                catalogue.append(travelPackage.toPrettyString());
                catalogue.append("\nDEPARTURES:\n");
                catalogue.append(departuresJson);
                catalogue.append("\n\n");
            }

            return catalogue.toString();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Unable to load catalogue data",
                    e
            );
        }
    }
}