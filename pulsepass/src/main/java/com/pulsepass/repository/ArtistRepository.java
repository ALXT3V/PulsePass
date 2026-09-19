package com.pulsepass.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulsepass.domain.Artist;

public interface ArtistRepository extends JpaRepository<Artist, Long> {

    Optional<Artist> findByStageName(String stageName);

    List<Artist> findByGenreIgnoreCase(String genre);

    List<Artist> findByCountryIgnoreCase(String country);
}
    