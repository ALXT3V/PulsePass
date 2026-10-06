package com.pulsepass.repository;

import com.pulsepass.BaseRepositoryTest;
import com.pulsepass.domain.Venue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class VenueRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private VenueRepository venueRepository;

    @Test
    @DisplayName("Debe guardar un Venue y luego buscarlo por código de negocio")
    void shouldSaveAndFindByCode() {
        Venue venue = new Venue("VNE-TEST-01", "Movistar Arena Test", "Bogotá", "Calle 63 #28-80", 14000, true);

        venueRepository.save(venue);
        Optional<Venue> found = venueRepository.findByCode("VNE-TEST-01");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Movistar Arena Test");
        assertThat(found.get().getCapacity()).isEqualTo(14000);
    }
}