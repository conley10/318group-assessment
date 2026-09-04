package com.travelassistant.travel_assistant_service.controller;

import com.travelassistant.travel_assistant_service.client.CatalogueClient;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/assistant")
public class TravelAssistantController {

    private final ChatModel chatModel;
    private final CatalogueClient catalogueClient;

    public TravelAssistantController(
            ChatModel chatModel,
            CatalogueClient catalogueClient) {

        this.chatModel = chatModel;
        this.catalogueClient = catalogueClient;
    }

    @PostMapping("/recommend")
    public Map<String, String> recommend(@RequestBody Map<String, String> request) {

        String message = request.get("message");

        String catalogueData = catalogueClient.getAllPackages();

        String prompt = """
                You are an AI travel assistant for a travel booking system.

                The customer has made the following request:

                %s

                Below are the travel packages currently available in our catalogue:

                %s

                Recommend the best matching package from the catalogue.

                Rules:
                - Only recommend packages that appear in the catalogue data.
                - Consider destination, budget, travel dates, number of travellers and preferences.
                - If no package is suitable, clearly say that no suitable package is currently available.
                - Explain briefly why the recommended package is the best match.
                - Do not invent travel packages.

                """.formatted(message, catalogueData);

        String response = chatModel.chat(prompt);

        return Map.of("response", response);
    }
}