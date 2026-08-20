package com.travelbooking.catalogue.controller;

import com.travelbooking.catalogue.model.TravelPackage;
import com.travelbooking.catalogue.service.TravelPackageService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TravelPackageController.class)
class TravelPackageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private TravelPackageService service;

    @Test
    void shouldGetAllPackages() throws Exception {

        TravelPackage travelPackage = examplePackage();

        when(service.getAllPackages())
                .thenReturn(List.of(travelPackage));

        mockMvc.perform(
                        get("/api/packages")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].packageId")
                        .value(1))
                .andExpect(jsonPath("$[0].name")
                        .value("Tokyo Adventure"))
                .andExpect(jsonPath("$[0].destination")
                        .value("Tokyo, Japan"));
    }

    @Test
    void shouldSearchPackagesByDestination() throws Exception {

        TravelPackage travelPackage = examplePackage();

        when(service.searchByDestination("Tokyo"))
                .thenReturn(List.of(travelPackage));

        mockMvc.perform(
                        get("/api/packages")
                                .param(
                                        "destination",
                                        "Tokyo"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].destination")
                        .value("Tokyo, Japan"));
    }

    @Test
    void shouldGetPackageById() throws Exception {

        TravelPackage travelPackage = examplePackage();

        when(service.getPackageById(1L))
                .thenReturn(
                        Optional.of(travelPackage)
                );

        mockMvc.perform(
                        get("/api/packages/{id}", 1L)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.packageId")
                        .value(1))
                .andExpect(jsonPath("$.name")
                        .value("Tokyo Adventure"));
    }

    @Test
    void shouldReturn404WhenPackageDoesNotExist()
            throws Exception {

        when(service.getPackageById(999L))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                        get("/api/packages/{id}", 999L)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreatePackage() throws Exception {

        TravelPackage request =
                new TravelPackage();

        request.setName("Tokyo Adventure");
        request.setDestination("Tokyo, Japan");
        request.setDescription(
                "Seven-day holiday package exploring Tokyo."
        );

        TravelPackage created =
                examplePackage();

        when(service.createPackage(
                any(TravelPackage.class)
        )).thenReturn(created);

        mockMvc.perform(
                        post("/api/packages")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        jsonMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "/api/packages/1"
                ))
                .andExpect(jsonPath("$.packageId")
                        .value(1))
                .andExpect(jsonPath("$.name")
                        .value("Tokyo Adventure"));
    }

    @Test
    void shouldRejectInvalidPackage() throws Exception {

        TravelPackage invalid =
                new TravelPackage();

        invalid.setName("");
        invalid.setDestination("");
        invalid.setDescription("");

        mockMvc.perform(
                        post("/api/packages")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        jsonMapper.writeValueAsString(
                                                invalid
                                        )
                                )
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdatePackage() throws Exception {

        TravelPackage updated =
                examplePackage();

        updated.setName(
                "Tokyo Premium Adventure"
        );

        when(service.updatePackage(
                any(Long.class),
                any(TravelPackage.class)
        )).thenReturn(
                Optional.of(updated)
        );

        mockMvc.perform(
                        put("/api/packages/{id}", 1L)
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        jsonMapper.writeValueAsString(
                                                updated
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name")
                        .value(
                                "Tokyo Premium Adventure"
                        ));
    }

    @Test
    void shouldDeletePackage() throws Exception {

        when(service.deletePackage(1L))
                .thenReturn(true);

        mockMvc.perform(
                        delete("/api/packages/{id}", 1L)
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenDeletingUnknownPackage()
            throws Exception {

        when(service.deletePackage(999L))
                .thenReturn(false);

        mockMvc.perform(
                        delete(
                                "/api/packages/{id}",
                                999L
                        )
                )
                .andExpect(status().isNotFound());
    }

    private TravelPackage examplePackage() {

        TravelPackage travelPackage =
                new TravelPackage();

        travelPackage.setPackageId(1L);
        travelPackage.setName(
                "Tokyo Adventure"
        );
        travelPackage.setDestination(
                "Tokyo, Japan"
        );
        travelPackage.setDescription(
                "Seven-day holiday package exploring Tokyo."
        );

        return travelPackage;
    }
}