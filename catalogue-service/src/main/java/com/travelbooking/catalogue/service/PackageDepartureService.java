package com.travelbooking.catalogue.service;

import com.travelbooking.catalogue.model.PackageDeparture;
import com.travelbooking.catalogue.model.TravelPackage;
import com.travelbooking.catalogue.repository.PackageDepartureRepository;
import com.travelbooking.catalogue.repository.TravelPackageRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PackageDepartureService {

    private final PackageDepartureRepository departureRepository;
    private final TravelPackageRepository packageRepository;

    public PackageDepartureService(
            PackageDepartureRepository departureRepository,
            TravelPackageRepository packageRepository
    ) {
        this.departureRepository = departureRepository;
        this.packageRepository = packageRepository;
    }

    public List<PackageDeparture> getDeparturesForPackage(Long packageId) {
        return departureRepository
                .findByTravelPackagePackageId(packageId);
    }

    public Optional<PackageDeparture> getDepartureById(Long departureId) {
        return departureRepository.findById(departureId);
    }

    public Optional<PackageDeparture> createDeparture(
            Long packageId,
            PackageDeparture departure
    ) {
        Optional<TravelPackage> travelPackage =
                packageRepository.findById(packageId);

        if (travelPackage.isEmpty()) {
            return Optional.empty();
        }

        departure.setDepartureId(null);
        departure.setTravelPackage(travelPackage.get());

        return Optional.of(departureRepository.save(departure));
    }

    public boolean deleteDeparture(Long departureId) {
        if (!departureRepository.existsById(departureId)) {
            return false;
        }

        departureRepository.deleteById(departureId);
        return true;
    }
}