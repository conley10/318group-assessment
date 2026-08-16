package com.travelbooking.catalogue.controller;

import com.travelbooking.catalogue.model.PackageDeparture;
import com.travelbooking.catalogue.service.PackageDepartureService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
public class PackageDepartureController {

    private final PackageDepartureService service;

    public PackageDepartureController(
            PackageDepartureService service
    ) {
        this.service = service;
    }

    @GetMapping("/packages/{packageId}/departures")
    public List<PackageDeparture> getDeparturesForPackage(
            @PathVariable Long packageId
    ) {
        return service.getDeparturesForPackage(packageId);
    }

    @GetMapping("/departures/{departureId}")
    public ResponseEntity<PackageDeparture> getDepartureById(
            @PathVariable Long departureId
    ) {
        return service.getDepartureById(departureId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/packages/{packageId}/departures")
    public ResponseEntity<PackageDeparture> createDeparture(
            @PathVariable Long packageId,
            @Valid @RequestBody PackageDeparture departure
    ) {
        return service.createDeparture(packageId, departure)
                .map(created -> ResponseEntity
                        .created(URI.create(
                                "/api/departures/"
                                        + created.getDepartureId()
                        ))
                        .body(created))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/departures/{departureId}")
    public ResponseEntity<Void> deleteDeparture(
            @PathVariable Long departureId
    ) {
        if (!service.deleteDeparture(departureId)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}