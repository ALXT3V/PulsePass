package com.pulsepass.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulsepass.domain.Artist;
import com.pulsepass.domain.Event;
import com.pulsepass.domain.Venue;
import com.pulsepass.domain.enums.EventStatus;
import com.pulsepass.dto.request.CreateEventRequest;
import com.pulsepass.dto.response.EventResponse;
import com.pulsepass.dto.response.EventSummaryResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.EventMapper;
import com.pulsepass.repository.ArtistRepository;
import com.pulsepass.repository.EventRepository;
import com.pulsepass.repository.VenueRepository;
import com.pulsepass.service.EventService;

@Service 
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final ArtistRepository artistRepository;
    private final EventMapper eventMapper;

    public EventServiceImpl(EventRepository eventRepository, VenueRepository venueRepository, ArtistRepository artistRepository, EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
        this.artistRepository = artistRepository;
        this.eventMapper = eventMapper;
    }

    @Override 
    @Transactional 
    public EventResponse create(CreateEventRequest request) {
        if (eventRepository.findByEventCode(request.eventCode()).isPresent()) {
            throw new DuplicateResourceException("Event already exists with code: " + request.eventCode());
        }

        Venue venue = venueRepository.findByCode(request.venueCode())
            .orElseThrow(() -> new ResourceNotFoundException("Venue not found with code: " + request.venueCode()));

        if (!Boolean.TRUE.equals(venue.getActive())) {
            throw new BusinessRuleException("Cannot create an event in an inactive venue: " + request.venueCode());
        }

        if (request.eventDate() != null && request.eventDate().isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException("Event date must be in the future");
        }

        if (request.minimumAge() != null && request.minimumAge() < 0) {
            throw new BusinessRuleException("Minimum age cannot be negative");
        }    
        
        Event event = eventMapper.toEntity(request);
        event.setVenue(venue);
        event.setStatus(EventStatus.DRAFT);
         event.setMinimumAge(request.minimumAge() != null ? request.minimumAge() : 0);


        Event savedEvent = eventRepository.save(event);
        return eventMapper.toResponse(savedEvent);
    }

    @Override 
    public EventResponse findByCode(String eventCode) {
        return eventRepository.findByEventCodeWithDetails(eventCode)
            .or(() -> eventRepository.findByEventCode(eventCode))
            .map(eventMapper::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Event not found with code: " + eventCode));
    }

    @Override 
    public List<EventSummaryResponse> findPublishedEvents() {
        return eventRepository.findByStatus(EventStatus.PUBLISHED)
            .stream()
            .map(eventMapper::toSummaryResponse)
            .toList();
    }

    @Override 
    @Transactional
    public EventResponse publish(String eventCode) {
        Event event = eventRepository.findByEventCode(eventCode)
            .orElseThrow(() -> new ResourceNotFoundException("Event not found with code: " + eventCode));
            
        if (event.getStatus() != EventStatus.DRAFT) {
            throw new BusinessRuleException("Only events in DRAFT status can be published. Current status: " + event.getStatus());
        }

        if (event.getEventDate() != null && event.getEventDate().toLocalDateTime().isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException("Cannot publish an event with past date");
        }

        if (event.getVenue() != null && !Boolean.TRUE.equals(event.getVenue().getActive())) {
            throw new BusinessRuleException("Cannot publish an event whose venue is inactive");
        }

        event.setStatus(EventStatus.PUBLISHED);
        Event updateEvent = eventRepository.save(event);
        return eventMapper.toResponse(updateEvent);
    }

    @Override 
    @Transactional 
    public EventResponse addArtist(String eventCode, Long artistId) {
        Event event = eventRepository.findByEventCode(eventCode)
            .orElseThrow(() -> new ResourceNotFoundException("Event not found with code: " + eventCode));

        if (event.getStatus() == EventStatus.CANCELLED || event.getStatus() == EventStatus.FINISHED) {
            throw new BusinessRuleException("Cannot add artist to an event that is " + event.getStatus());
        }

        Artist artist = artistRepository.findById(artistId)
            .orElseThrow(() -> new ResourceNotFoundException("Artist not found with id: " + artistId));    
            
        if (event.getArtists().contains(artist)) {
            throw new DuplicateResourceException("Artist is already associated with this event");
        }
        
        event.getArtists().add(artist);
        Event updateEvent = eventRepository.save(event);
        return eventMapper.toResponse(updateEvent);
    }

    @Override 
    public List<EventSummaryResponse> findByArtist(String stageName) {
        return eventRepository.findAll()
            .stream()
            .filter(event -> event.getArtists() != null && event.getArtists().stream()
                .anyMatch(artist -> artist.getStageName() != null && artist.getStageName().equalsIgnoreCase(stageName)))
            .map(eventMapper::toSummaryResponse)
            .toList();    
    }
}