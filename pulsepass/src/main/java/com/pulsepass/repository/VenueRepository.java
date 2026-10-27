package com.pulsepass.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulsepass.domain.Venue;


public interface VenueRepository extends JpaRepository<Venue, Long> {

    Optional<Venue> findByCode(String code);

    List<Venue> findByCityIgnoreCase(String city);

    List<Venue> findByActiveTrue();
}
