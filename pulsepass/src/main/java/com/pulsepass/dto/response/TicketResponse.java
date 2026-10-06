package com.pulsepass.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.pulsepass.domain.enums.TicketStatus;
import com.pulsepass.domain.enums.TicketType;

public record TicketResponse (
    Long id,
    String ticketCode,
    TicketType type, 
    BigDecimal price,
    TicketStatus status,
    LocalDateTime purchaseDate,
    String userEmail,
    String eventCode,
    String eventName
) {
    
}
