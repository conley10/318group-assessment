package com.travelbooking.catalogue.repository;

import com.travelbooking.catalogue.model.TravelPackage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TravelPackageRepository
        extends JpaRepository<TravelPackage, Long> {

    List<TravelPackage> findByDestinationContainingIgnoreCase(
            String destination
    );
}