package com.pulsepass.dto.response;

import java.time.LocalDateTime;

import com.pulsepass.domain.enums.EventCategory;
import com.pulsepass.domain.enums.EventStatus;

public record EventSummaryResponse(
    Long id,
    String eventCode,
    String name,
    EventCategory category,
    EventStatus status,
    LocalDateTime eventDate,
    String venueName
) {
    
}
