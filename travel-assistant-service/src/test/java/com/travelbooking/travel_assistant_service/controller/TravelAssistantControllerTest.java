package com.travelassistant.travel_assistant_service.controller;

import com.travelassistant.travel_assistant_service.client.CatalogueClient;
import dev.langchain4j.model.chat.ChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TravelAssistantController.class)
class TravelAssistantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatModel chatModel;

    @MockitoBean
    private CatalogueClient catalogueClient;

    @Test
    void recommendReturnsAiRecommendation() throws Exception {

        when(catalogueClient.getAllPackages())
                .thenReturn("""
                        [
                          {
                            "packageId": 1,
                            "name": "Japan Winter Escape",
                            "destination": "Japan",
                            "description": "A 7-day winter holiday in Japan."
                          }
                        ]
                        """);

        when(chatModel.chat(anyString()))
                .thenReturn("I recommend the Japan Winter Escape package.");

        mockMvc.perform(post("/api/assistant/recommend")
                        .contentType("application/json")
                        .content("""
                                {
                                  "message": "I want a 7 day trip to Japan in December."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response")
                        .value("I recommend the Japan Winter Escape package."));

        verify(catalogueClient).getAllPackages();
        verify(chatModel).chat(anyString());
    }
}