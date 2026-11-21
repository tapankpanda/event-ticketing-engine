package com.tapan.eventticketingengine.event.service;

import com.tapan.eventticketingengine.event.dto.CreateEventRequest;
import com.tapan.eventticketingengine.event.dto.EventResponse;
import com.tapan.eventticketingengine.event.dto.UpdateEventRequest;
import com.tapan.eventticketingengine.event.entity.Event;
import com.tapan.eventticketingengine.event.enums.EventStatus;
import com.tapan.eventticketingengine.event.exception.EventNotDraftException;
import com.tapan.eventticketingengine.event.exception.EventNotFoundException;
import com.tapan.eventticketingengine.event.repository.EventRepository;
import com.tapan.eventticketingengine.user.entity.User;
import com.tapan.eventticketingengine.user.enums.UserRole;
import com.tapan.eventticketingengine.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EventService(
            EventRepository eventRepository,
            UserRepository userRepository
    ) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    private EventResponse toResponse(Event event) {
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getVenue(),
                event.getStartAt(),
                event.getEndAt(),
                event.getStatus(),
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }

    private User getOrganizer(String organizerEmail) {
        User organizer = userRepository.findByEmail(organizerEmail)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Authenticated organizer not found")
                );

        if (organizer.getRole() != UserRole.ORGANIZER) {
            throw new IllegalStateException("Only organizers can manage events");
        }

        return organizer;
    }

    @Transactional
    public EventResponse createEvent(
            CreateEventRequest request,
            String organizerEmail
    ) {
        User organizer = getOrganizer(organizerEmail);

        Event event = new Event(
                request.title(),
                request.description(),
                request.venue(),
                request.startAt(),
                request.endAt(),
                organizer
        );

        Event savedEvent = eventRepository.save(event);

        return toResponse(savedEvent);
    }

    @Transactional
    public EventResponse updateEvent(
            Long eventId,
            UpdateEventRequest request,
            String organizerEmail
    ) {
        User organizer = getOrganizer(organizerEmail);

        Event event = eventRepository
                .findByIdAndOrganizer_Id(eventId, organizer.getId())
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new EventNotDraftException(eventId);
        }

        event.updateDetails(
                request.title(),
                request.description(),
                request.venue(),
                request.startAt(),
                request.endAt()
        );

        eventRepository.flush();

        return toResponse(event);
    }

    @Transactional
    public EventResponse publishEvent(
            Long eventId,
            String organizerEmail
    ) {
        User organizer = getOrganizer(organizerEmail);

        Event event = eventRepository
                .findByIdAndOrganizer_Id(eventId, organizer.getId())
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new EventNotDraftException(eventId);
        }

        event.publish();

        eventRepository.flush();

        return toResponse(event);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getAllPublishedEvents() {
        return eventRepository
                .findAllByStatusOrderByStartAtAsc(EventStatus.PUBLISHED)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EventResponse getPublishedEvent(Long eventId) {
        Event event = eventRepository
                .findByIdAndStatus(eventId, EventStatus.PUBLISHED)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        return toResponse(event);
    }
}
