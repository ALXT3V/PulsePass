package com.pulsepass.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pulsepass.domain.Ticket;
import com.pulsepass.domain.enums.TicketStatus;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByTicketCode(String ticketCode);

    List<Ticket> findByUserId(Long userId);

    List<Ticket> findByEventId(Long eventId);

    List<Ticket> findByStatus(TicketStatus status);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.event.id = :eventId AND t.status = 'PAID'")
    Long countPaidTicketsByEventId(@Param("eventId") Long eventId);

    @Query("SELECT COALESCE(SUM(t.price), 0) FROM Ticket t WHERE t.event.id = :eventId AND t.status = 'PAID'")
    BigDecimal calculateTotalRevenueByEventId(@Param("eventId") Long eventId);
}