package com.pulsepass.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pulsepass.domain.Event;
import com.pulsepass.domain.enums.EventCategory;
import com.pulsepass.domain.enums.EventStatus;

public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByEventCode(String eventCode);

    List<Event> findByStatus(EventStatus status);

    List<Event> findByCategory(EventCategory category);

    List<Event> findByEventDateBetween(OffsetDateTime start, OffsetDateTime end);

    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.venue LEFT JOIN FETCH e.artists WHERE e.eventCode = :eventCode")
    Optional<Event> findByEventCodeWithDetails(@Param("eventCode") String eventCode);

    @Query("SELECT e FROM Event e WHERE e.category = :category AND e.status = :status ORDER BY e.eventDate ASC")
    List<Event> findByCategoryAndStatus(
        @Param("category") EventCategory category,
        @Param("status") EventStatus status
    );
}
