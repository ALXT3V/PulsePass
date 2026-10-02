package com.pulsepass.dto.request;

import java.time.LocalDateTime;

import com.pulsepass.domain.enums.EventCategory;


public record CreateEventRequest (
    String eventCode,
    String name,
    String description,
    EventCategory category,
    LocalDateTime eventDate,
    Integer minimunAge,
    String venueCode
){
    
}
