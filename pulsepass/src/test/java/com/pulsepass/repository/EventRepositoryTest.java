package com.pulsepass.repository;

import com.pulsepass.BaseRepositoryTest;
import com.pulsepass.domain.Event;
import com.pulsepass.domain.Venue;
import com.pulsepass.domain.enums.EventCategory;
import com.pulsepass.domain.enums.EventStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class EventRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Test
    @DisplayName("Debe guardar un Evento y buscarlo por eventCode con detalles")
    void shouldSaveAndFindByEventCodeWithDetails() {

        Venue venue = venueRepository.save(new Venue("VNE-MED-01", "Atanasio Girardot", "Medellín", "Cra 74", 45000, true));

        Event event = new Event(
                "EVT-ROCK-01",
                "Rock Fest 2026",
                "Festival de Rock",
                EventCategory.MUSIC,
                EventStatus.PUBLISHED,
                OffsetDateTime.now().plusDays(10),
                18,
                venue
        );
        eventRepository.save(event);

        Optional<Event> found = eventRepository.findByEventCodeWithDetails("EVT-ROCK-01");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Rock Fest 2026");
        assertThat(found.get().getVenue().getName()).isEqualTo("Atanasio Girardot");
    }
}