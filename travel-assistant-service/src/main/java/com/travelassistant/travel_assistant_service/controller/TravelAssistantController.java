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
        You are an AI travel assistant for the Voyage travel booking system.

        CUSTOMER REQUEST:
        %s

        LIVE CATALOGUE DATA:
        %s

        Your job is to recommend the best matching travel package
        using ONLY the live catalogue data above.

        IMPORTANT:
        - Each package may contain departure information.
        - Read departure prices when evaluating the customer's budget.
        - Prices in departure data are per person.
        - Read departure startDate and endDate when evaluating travel dates.
        - Read capacity or availableCapacity when considering traveller numbers.
        - Only consider departures with status AVAILABLE.
        - Never invent a package, price, date or availability.
        - If the customer gives a maximum budget, do not recommend a departure above that budget.
        - If multiple packages match, choose the strongest match based on the customer's preferences.
        - If no package satisfies the customer's requirements, clearly say so.
        - Keep your response concise and customer-friendly.

        When recommending a package, include:
        - Package name
        - Destination
        - Available departure dates, if provided
        - Price per person, if provided
        - A short explanation of why it matches

        """.formatted(message, catalogueData);

        String response = chatModel.chat(prompt);

        return Map.of("response", response);
    }
}