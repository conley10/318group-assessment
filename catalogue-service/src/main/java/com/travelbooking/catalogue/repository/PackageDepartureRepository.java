package com.travelbooking.catalogue.repository;

import com.travelbooking.catalogue.model.PackageDeparture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PackageDepartureRepository
        extends JpaRepository<PackageDeparture, Long> {

    List<PackageDeparture> findByTravelPackagePackageId(Long packageId);
}