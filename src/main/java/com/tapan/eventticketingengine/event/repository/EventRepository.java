package com.tapan.eventticketingengine.event.repository;

import com.tapan.eventticketingengine.event.entity.Event;
import com.tapan.eventticketingengine.event.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findAllByStatusOrderByStartAtAsc(EventStatus status);

    Optional<Event> findByIdAndStatus(Long id, EventStatus status);

    Optional<Event> findByIdAndOrganizer_Id(Long id, Long organizerId);
}
