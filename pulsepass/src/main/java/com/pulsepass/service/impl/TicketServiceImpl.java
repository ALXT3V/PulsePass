package com.pulsepass.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulsepass.domain.Event;
import com.pulsepass.domain.Ticket;
import com.pulsepass.domain.User;
import com.pulsepass.domain.enums.EventStatus;
import com.pulsepass.domain.enums.TicketStatus;
import com.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.dto.response.TicketResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.TicketMapper;
import com.pulsepass.repository.EventRepository;
import com.pulsepass.repository.TicketRepository;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.service.TicketService;

@Service 
@Transactional (readOnly = true)
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final TicketMapper ticketMapper; 
    

    public TicketServiceImpl(TicketRepository ticketRepository, UserRepository userRepository, EventRepository eventRepository, TicketMapper ticketMapper){
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.ticketMapper = ticketMapper;
    }

    @Override 
    @Transactional
    public TicketResponse purchase(PurchaseTicketRequest request){
        User user = userRepository.findByEmail(request.userEmail())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: "+request.userEmail()));

    
        if(!Boolean.TRUE.equals(user.getActive())){
            throw new BusinessRuleException("Inactive user cannot purchase tickets");
        }

        Event event = eventRepository.findByEventCode(request.eventCode())
            .orElseThrow(() -> new ResourceNotFoundException("Event not found with code: " + request.eventCode()));

        if(event.getStatus() != EventStatus.PUBLISHED){
            throw new BusinessRuleException("Tickets can only be purchased for PUBLISHED events. Current status: " + event.getStatus());

        }
        
        LocalDateTime now = LocalDateTime.now();
        if(event.getEventCode() != null && event.getEventDate().toLocalDateTime().isBefore(now)){
            throw new BusinessRuleException("Cannot purchase tickets for an event that has already ocurred");

        }
        
        if(event.getMinimumAge() != null && event.getMinimumAge() >0){
            if(user.getProfile() == null || user.getProfile().getBirthDate() == null ){
                throw new BusinessRuleException("User birth date is required to verify age requirement");

            }
            LocalDate eventDate = event.getEventDate() != null ? event.getEventDate().toLocalDate() : LocalDate.now();
            int age = Period.between(user.getProfile().getBirthDate(), eventDate).getYears();
            if(age < event.getMinimumAge()){
                throw new BusinessRuleException("User does not meet the minimum age requirement of " + event.getMinimumAge());

            }

        }

        Long paidTicket = ticketRepository.countPaidTicketsByEventId(event.getId());
        if (paidTicket == null){
            paidTicket = 0L;
        }

        Integer capacity = event.getVenue() != null ? event.getVenue().getCapacity() :0;
        if(paidTicket >= capacity ){
            throw new BusinessRuleException("Event has reached maximum venue capacity");
        }

        Ticket ticket = new Ticket();
        ticket.setTicketCode("TCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        ticket.setUser(user);
        ticket.setEvent(event);
        ticket.setType(request.type());
        ticket.setStatus(TicketStatus.PAID);
        ticket.setPurchaseDate(java.time.OffsetDateTime.now());

        BigDecimal price = BigDecimal.valueOf(100);
        ticket.setPrice(price);

        Ticket savedTicket = ticketRepository.save(ticket);

        if(paidTicket + 1 >= capacity){
            event.setStatus(EventStatus.SOLD_OUT);
            eventRepository.save(event);

        }
        return ticketMapper.toResponse(savedTicket);
    }


    @Override 
    public TicketResponse findByCode(String ticketCode){
        return ticketRepository.findByTicketCode(ticketCode)
            .map(ticketMapper :: toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with code: "+ticketCode));
    }

    @Override 
    public List<TicketResponse> findByUserEmail(String email){
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: "+ email));

        return ticketRepository.findByUserId(user.getId())
            .stream()
            .map(ticketMapper :: toResponse)
            .toList();        
    }

    @Override 
    public List<TicketResponse> findPaidTicketsByEvent(String eventCode){
        Event event = eventRepository.findByEventCode(eventCode)
            .orElseThrow(() -> new ResourceNotFoundException("Event not found with code: "+ eventCode));

        return ticketRepository.findByEventId(event.getId())
            .stream()
            .filter(ticket -> ticket.getStatus() == TicketStatus.PAID)
            .map(ticketMapper :: toResponse)
            .toList();        
    }

    @Override 
    @Transactional 
    public TicketResponse cancel(String ticketCode){
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
            .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with code: "+ticketCode));

        
        if(ticket.getStatus() != TicketStatus.PAID){
            throw new BusinessRuleException("Only PAID tickets can be cancelled. Current status: "+ticket.getStatus());

        }   
        
        if(ticket.getEvent() != null && ticket.getEvent().getEventDate() != null && ticket.getEvent().getEventDate().toLocalDateTime().isBefore(LocalDateTime.now())){
            throw new BusinessRuleException("Cannot cancel a ticket for an event that has already occurred");
        }

        ticket.setStatus(TicketStatus.CANCELLED);
        Ticket updateTicket = ticketRepository.save(ticket);
        return ticketMapper.toResponse(updateTicket);
    }
    @Override
    @Transactional
    public TicketResponse markAsUsed(String ticketCode) {
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with code: " + ticketCode));

        if (ticket.getStatus() != TicketStatus.PAID) {
            throw new BusinessRuleException("Only PAID tickets can be marked as used. Current status: " + ticket.getStatus());
        }

        ticket.setStatus(TicketStatus.USED);
        Ticket updatedTicket = ticketRepository.save(ticket);
        return ticketMapper.toResponse(updatedTicket);

}
}
