package com.pulsepass.dto.response;

import java.time.LocalDateTime;
import java.util.Set;

import com.pulsepass.domain.enums.EventCategory;
import com.pulsepass.domain.enums.EventStatus;

public record EventResponse(
    Long id,
    String eventCode,
    String name,
    String description,
    EventCategory category,
    EventStatus status,
    LocalDateTime eventDate,
    Integer minimumAge,
    String venueCode,
    String venueName,
    Set<ArtistResponse> artists
) {
    
}
