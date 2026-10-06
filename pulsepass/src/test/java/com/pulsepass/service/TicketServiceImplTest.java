package com.pulsepass.service;

import com.pulsepass.domain.Event;
import com.pulsepass.domain.Ticket;
import com.pulsepass.domain.User;
import com.pulsepass.domain.UserProfile;
import com.pulsepass.domain.Venue;
import com.pulsepass.domain.enums.EventStatus;
import com.pulsepass.domain.enums.TicketStatus;
import com.pulsepass.domain.enums.TicketType;
import com.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.dto.response.TicketResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.mapper.TicketMapper;
import com.pulsepass.repository.EventRepository;
import com.pulsepass.repository.TicketRepository;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.service.impl.TicketServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private TicketMapper ticketMapper;

    @InjectMocks
    private TicketServiceImpl ticketService;

    @Test
    @DisplayName("TEST-TICKET-001: Compra valida de ticket guarda y retorna TicketResponse")
    void purchase_ValidRequest_SavesTicketAndReturnsResponse() {
        PurchaseTicketRequest request = new PurchaseTicketRequest("user@email.com", "CMF-2026", TicketType.GENERAL);

        User user = new User();
        user.setActive(true);
        UserProfile profile = new UserProfile();
        profile.setBirthDate(LocalDate.of(2000, 1, 1));
        user.setProfile(profile);

        Venue venue = new Venue();
        venue.setCapacity(100);

        Event event = new Event();
        event.setStatus(EventStatus.PUBLISHED);
        event.setEventDate(OffsetDateTime.now().plusDays(10));
        event.setMinimumAge(18);
        event.setVenue(venue);

        Ticket savedTicket = new Ticket();
        TicketResponse expectedResponse = new TicketResponse(
                1L,
                "TCK-12345678",
                TicketType.GENERAL,
                BigDecimal.valueOf(100),
                TicketStatus.PAID,
                LocalDateTime.now(),
                "user@email.com",
                "CMF-2026",
                "Caribbean Fest"
        );

        when(userRepository.findByEmail(request.userEmail())).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode(request.eventCode())).thenReturn(Optional.of(event));
        when(ticketRepository.countPaidTicketsByEventId(event.getId())).thenReturn(10L);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);
        when(ticketMapper.toResponse(savedTicket)).thenReturn(expectedResponse);

        TicketResponse result = ticketService.purchase(request);

        assertThat(result).isNotNull();
        assertThat(result.ticketCode()).isEqualTo("TCK-12345678");
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    @DisplayName("TEST-TICKET-002: Compra con usuario inactivo lanza BusinessRuleException")
    void purchase_InactiveUser_ThrowsBusinessRuleException() {
        PurchaseTicketRequest request = new PurchaseTicketRequest("user@email.com", "CMF-2026", TicketType.GENERAL);

        User user = new User();
        user.setActive(false);

        when(userRepository.findByEmail(request.userEmail())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> ticketService.purchase(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Inactive user cannot purchase tickets");

        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("TEST-TICKET-003: Compra para evento no publicado lanza BusinessRuleException")
    void purchase_UnpublishedEvent_ThrowsBusinessRuleException() {
        PurchaseTicketRequest request = new PurchaseTicketRequest("user@email.com", "CMF-2026", TicketType.GENERAL);

        User user = new User();
        user.setActive(true);

        Event event = new Event();
        event.setStatus(EventStatus.DRAFT);

        when(userRepository.findByEmail(request.userEmail())).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode(request.eventCode())).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> ticketService.purchase(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Tickets can only be purchased for PUBLISHED events");

        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("TEST-TICKET-004: Compra por usuario menor de edad lanza BusinessRuleException")
    void purchase_UnderageUser_ThrowsBusinessRuleException() {
        PurchaseTicketRequest request = new PurchaseTicketRequest("user@email.com", "CMF-2026", TicketType.GENERAL);

        User user = new User();
        user.setActive(true);
        UserProfile profile = new UserProfile();
        profile.setBirthDate(LocalDate.now().minusYears(15));
        user.setProfile(profile);

        Event event = new Event();
        event.setStatus(EventStatus.PUBLISHED);
        event.setEventDate(OffsetDateTime.now().plusDays(10));
        event.setMinimumAge(18);

        when(userRepository.findByEmail(request.userEmail())).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode(request.eventCode())).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> ticketService.purchase(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("User does not meet the minimum age requirement");

        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("TEST-TICKET-005: Compra en evento con capacidad maxima alcanzada lanza BusinessRuleException")
    void purchase_MaxCapacityReached_ThrowsBusinessRuleException() {
        PurchaseTicketRequest request = new PurchaseTicketRequest("user@email.com", "CMF-2026", TicketType.GENERAL);

        User user = new User();
        user.setActive(true);

        Venue venue = new Venue();
        venue.setCapacity(50);

        Event event = new Event();
        event.setStatus(EventStatus.PUBLISHED);
        event.setEventDate(OffsetDateTime.now().plusDays(10));
        event.setVenue(venue);

        when(userRepository.findByEmail(request.userEmail())).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode(request.eventCode())).thenReturn(Optional.of(event));
        when(ticketRepository.countPaidTicketsByEventId(event.getId())).thenReturn(50L);

        assertThatThrownBy(() -> ticketService.purchase(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Event has reached maximum venue capacity");

        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("TEST-TICKET-006: Compra que llena capacidad cambia estado del evento a SOLD_OUT")
    void purchase_FillsCapacity_ChangesEventStatusToSoldOut() {
        PurchaseTicketRequest request = new PurchaseTicketRequest("user@email.com", "CMF-2026", TicketType.GENERAL);

        User user = new User();
        user.setActive(true);

        Venue venue = new Venue();
        venue.setCapacity(10);

        Event event = new Event();
        event.setStatus(EventStatus.PUBLISHED);
        event.setEventDate(OffsetDateTime.now().plusDays(10));
        event.setVenue(venue);

        Ticket savedTicket = new Ticket();

        when(userRepository.findByEmail(request.userEmail())).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode(request.eventCode())).thenReturn(Optional.of(event));
        when(ticketRepository.countPaidTicketsByEventId(event.getId())).thenReturn(9L);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);

        ticketService.purchase(request);

        assertThat(event.getStatus()).isEqualTo(EventStatus.SOLD_OUT);
        verify(eventRepository).save(event);
    }

    @Test
    @DisplayName("TEST-TICKET-007: Cancelar ticket PAID cambia su estado a CANCELLED")
    void cancel_PaidTicket_ChangesStatusToCancelled() {
        String ticketCode = "TCK-12345678";
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.PAID);

        Event event = new Event();
        event.setEventDate(OffsetDateTime.now().plusDays(5));
        ticket.setEvent(event);

        Ticket updatedTicket = new Ticket();
        updatedTicket.setStatus(TicketStatus.CANCELLED);

        TicketResponse expectedResponse = new TicketResponse(
                1L,
                ticketCode,
                TicketType.GENERAL,
                BigDecimal.valueOf(100),
                TicketStatus.CANCELLED,
                LocalDateTime.now(),
                "user@email.com",
                "CMF-2026",
                "Caribbean Fest"
        );

        when(ticketRepository.findByTicketCode(ticketCode)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(updatedTicket);
        when(ticketMapper.toResponse(updatedTicket)).thenReturn(expectedResponse);

        TicketResponse result = ticketService.cancel(ticketCode);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(TicketStatus.CANCELLED);
        verify(ticketRepository).save(ticket);
    }

    @Test
    @DisplayName("TEST-TICKET-008: Cancelar ticket no PAID lanza BusinessRuleException")
    void cancel_NonPaidTicket_ThrowsBusinessRuleException() {
        String ticketCode = "TCK-12345678";
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.USED);

        when(ticketRepository.findByTicketCode(ticketCode)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.cancel(ticketCode))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Only PAID tickets can be cancelled");

        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("TEST-TICKET-009: Marcar como usado ticket PAID cambia su estado a USED")
    void markAsUsed_PaidTicket_ChangesStatusToUsed() {
        String ticketCode = "TCK-12345678";
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.PAID);

        Ticket updatedTicket = new Ticket();
        updatedTicket.setStatus(TicketStatus.USED);

        TicketResponse expectedResponse = new TicketResponse(
                1L,
                ticketCode,
                TicketType.GENERAL,
                BigDecimal.valueOf(100),
                TicketStatus.USED,
                LocalDateTime.now(),
                "user@email.com",
                "CMF-2026",
                "Caribbean Fest"
        );

        when(ticketRepository.findByTicketCode(ticketCode)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(updatedTicket);
        when(ticketMapper.toResponse(updatedTicket)).thenReturn(expectedResponse);

        TicketResponse result = ticketService.markAsUsed(ticketCode);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(TicketStatus.USED);
        verify(ticketRepository).save(ticket);
    }
}