package com.travelbooking.catalogue.controller;

import com.travelbooking.catalogue.model.TravelPackage;
import com.travelbooking.catalogue.service.TravelPackageService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/packages")
public class TravelPackageController {

    private final TravelPackageService service;

    public TravelPackageController(TravelPackageService service) {
        this.service = service;
    }

   @GetMapping
public List<TravelPackage> getAllPackages(
        @RequestParam(required = false) String destination
) {
    if (destination != null && !destination.isBlank()) {
        return service.searchByDestination(destination);
    }

    return service.getAllPackages();
}

    @GetMapping("/{id}")
    public ResponseEntity<TravelPackage> getPackageById(
            @PathVariable Long id
    ) {
        return service.getPackageById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TravelPackage> createPackage(
            @Valid @RequestBody TravelPackage travelPackage
    ) {
        TravelPackage created = service.createPackage(travelPackage);

        return ResponseEntity
                .created(URI.create(
                        "/api/packages/" + created.getPackageId()
                ))
                .body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TravelPackage> updatePackage(
            @PathVariable Long id,
            @Valid @RequestBody TravelPackage travelPackage
    ) {
        return service.updatePackage(id, travelPackage)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePackage(
            @PathVariable Long id
    ) {
        if (!service.deletePackage(id)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}