
package com.pulsepass.service;

import com.pulsepass.domain.Event;
import com.pulsepass.domain.Venue;
import com.pulsepass.domain.enums.EventCategory;
import com.pulsepass.domain.enums.EventStatus;
import com.pulsepass.dto.request.CreateEventRequest;
import com.pulsepass.dto.response.EventResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.EventMapper;
import com.pulsepass.repository.ArtistRepository;
import com.pulsepass.repository.EventRepository;
import com.pulsepass.repository.VenueRepository;
import com.pulsepass.service.impl.EventServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private VenueRepository venueRepository;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventServiceImpl eventService;

    @Test
    @DisplayName("TEST-EVENT-001: Evento existente retorna DTO")
    void findByCode_ExistingEvent_ReturnsEventResponse() {
        String eventCode = "CMF-2026";
        Event event = new Event();
        event.setEventCode(eventCode);

        EventResponse expectedResponse = new EventResponse(
                1L, eventCode, "Caribbean Music Fest 2026", "Festival de música",
                EventCategory.MUSIC, EventStatus.DRAFT, LocalDateTime.now().plusDays(30),
                18, "VEN-SMR-01", "Marina Convention Center", Collections.emptySet()
        );

        when(eventRepository.findByEventCodeWithDetails(eventCode)).thenReturn(Optional.of(event));
        when(eventMapper.toResponse(event)).thenReturn(expectedResponse);

        EventResponse result = eventService.findByCode(eventCode);

        assertThat(result).isNotNull();
        assertThat(result.eventCode()).isEqualTo(eventCode);
        verify(eventRepository).findByEventCodeWithDetails(eventCode);
    }

    @Test
    @DisplayName("TEST-EVENT-002: Evento inexistente lanza ResourceNotFoundException")
    void findByCode_NonExistingEvent_ThrowsResourceNotFoundException() {
        String eventCode = "UNKNOWN";
        when(eventRepository.findByEventCodeWithDetails(eventCode)).thenReturn(Optional.empty());
        when(eventRepository.findByEventCode(eventCode)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.findByCode(eventCode))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Event not found with code");
    }

    @Test
    @DisplayName("TEST-EVENT-003: Crear evento valido ejecuta save()")
    void create_ValidEvent_SavesEvent() {
        CreateEventRequest request = new CreateEventRequest(
                "CMF-2026", "Caribbean Music Fest 2026", "Festival",
                EventCategory.MUSIC, LocalDateTime.now().plusDays(30), 18, "VEN-SMR-01"
        );

        Venue venue = new Venue();
        venue.setCode("VEN-SMR-01");
        venue.setActive(true);

        Event eventEntity = new Event();
        Event savedEvent = new Event();

        EventResponse expectedResponse = new EventResponse(
                1L, "CMF-2026", "Caribbean Music Fest 2026", "Festival",
                EventCategory.MUSIC, EventStatus.DRAFT, LocalDateTime.now().plusDays(30),
                18, "VEN-SMR-01", "Marina Convention Center", Collections.emptySet()
        );

        when(eventRepository.findByEventCode(request.eventCode())).thenReturn(Optional.empty());
        when(venueRepository.findByCode(request.venueCode())).thenReturn(Optional.of(venue));
        when(eventMapper.toEntity(request)).thenReturn(eventEntity);
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);
        when(eventMapper.toResponse(savedEvent)).thenReturn(expectedResponse);

        EventResponse result = eventService.create(request);

        assertThat(result).isNotNull();
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    @DisplayName("TEST-EVENT-004: Venue inexistente lanza ResourceNotFoundException y save() nunca se ejecuta")
    void create_NonExistingVenue_ThrowsResourceNotFoundException() {
        CreateEventRequest request = new CreateEventRequest(
                "CMF-2026", "Caribbean Music Fest 2026", "Festival",
                EventCategory.MUSIC, LocalDateTime.now().plusDays(30), 18, "INVALID-VENUE"
        );

        when(eventRepository.findByEventCode(request.eventCode())).thenReturn(Optional.empty());
        when(venueRepository.findByCode(request.venueCode())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Venue not found with code");

        verify(eventRepository, never()).save(any());
    }

    @Test
    @DisplayName("TEST-EVENT-005: Venue inactivo lanza BusinessRuleException")
    void create_InactiveVenue_ThrowsBusinessRuleException() {
        CreateEventRequest request = new CreateEventRequest(
                "CMF-2026", "Caribbean Music Fest 2026", "Festival",
                EventCategory.MUSIC, LocalDateTime.now().plusDays(30), 18, "VEN-SMR-01"
        );

        Venue venue = new Venue();
        venue.setCode("VEN-SMR-01");
        venue.setActive(false);

        when(eventRepository.findByEventCode(request.eventCode())).thenReturn(Optional.empty());
        when(venueRepository.findByCode(request.venueCode())).thenReturn(Optional.of(venue));

        assertThatThrownBy(() -> eventService.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Cannot create an event in an inactive venue");

        verify(eventRepository, never()).save(any());
    }

    @Test
    @DisplayName("TEST-EVENT-006: Fecha pasada lanza BusinessRuleException")
    void create_PastDate_ThrowsBusinessRuleException() {
        CreateEventRequest request = new CreateEventRequest(
                "CMF-2026", "Caribbean Music Fest 2026", "Festival",
                EventCategory.MUSIC, LocalDateTime.now().minusDays(1), 18, "VEN-SMR-01"
        );

        when(eventRepository.findByEventCode(request.eventCode())).thenReturn(Optional.empty());
        when(venueRepository.findByCode(request.venueCode())).thenReturn(Optional.of(new Venue()));

        assertThatThrownBy(() -> eventService.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Event date must be in the future");

        verify(eventRepository, never()).save(any());
    }

    @Test
    @DisplayName("TEST-EVENT-007: Publicar DRAFT valido cambia estado a PUBLISHED")
    void publish_DraftEvent_ChangesStatusToPublished() {
        String eventCode = "CMF-2026";
        Event event = new Event();
        event.setEventCode(eventCode);
        event.setStatus(EventStatus.DRAFT);
        event.setEventDate(OffsetDateTime.now().plusDays(10));

        Venue venue = new Venue();
        venue.setActive(true);
        event.setVenue(venue);

        Event updatedEvent = new Event();
        updatedEvent.setStatus(EventStatus.PUBLISHED);

        EventResponse expectedResponse = new EventResponse(
                1L, eventCode, "Caribbean Music Fest 2026", "Festival",
                EventCategory.MUSIC, EventStatus.PUBLISHED, LocalDateTime.now().plusDays(10),
                18, "VEN-SMR-01", "Marina Convention Center", Collections.emptySet()
        );

        when(eventRepository.findByEventCode(eventCode)).thenReturn(Optional.of(event));
        when(eventRepository.save(event)).thenReturn(updatedEvent);
        when(eventMapper.toResponse(updatedEvent)).thenReturn(expectedResponse);

        EventResponse result = eventService.publish(eventCode);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(EventStatus.PUBLISHED);
        verify(eventRepository).save(event);
    }

    @Test
    @DisplayName("TEST-EVENT-008: Publicar CANCELLED lanza BusinessRuleException y no persiste")
    void publish_CancelledEvent_ThrowsBusinessRuleException() {
        String eventCode = "CMF-2026";
        Event event = new Event();
        event.setEventCode(eventCode);
        event.setStatus(EventStatus.CANCELLED);

        when(eventRepository.findByEventCode(eventCode)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> eventService.publish(eventCode))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Only events in DRAFT status can be published");

        verify(eventRepository, never()).save(any());
    }
}