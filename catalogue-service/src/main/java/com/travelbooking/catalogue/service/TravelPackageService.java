package com.travelbooking.catalogue.service;

import com.travelbooking.catalogue.model.TravelPackage;
import com.travelbooking.catalogue.repository.TravelPackageRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TravelPackageService {

    private final TravelPackageRepository repository;

    public TravelPackageService(TravelPackageRepository repository) {
        this.repository = repository;
    }

    public List<TravelPackage> getAllPackages() {
        return repository.findAll();
    }

    public Optional<TravelPackage> getPackageById(Long id) {
        return repository.findById(id);
    }

    public List<TravelPackage> searchByDestination(String destination) {
        return repository.findByDestinationContainingIgnoreCase(destination);
    }

    public TravelPackage createPackage(TravelPackage travelPackage) {
        travelPackage.setPackageId(null);
        return repository.save(travelPackage);
    }

    public Optional<TravelPackage> updatePackage(
            Long id,
            TravelPackage updatedPackage
    ) {
        return repository.findById(id)
                .map(existingPackage -> {
                    existingPackage.setName(updatedPackage.getName());
                    existingPackage.setDestination(
                            updatedPackage.getDestination()
                    );
                    existingPackage.setDescription(
                            updatedPackage.getDescription()
                    );

                    return repository.save(existingPackage);
                });
    }

    public boolean deletePackage(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }

        repository.deleteById(id);
        return true;
    }
}