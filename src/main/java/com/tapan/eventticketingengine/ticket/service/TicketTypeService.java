package com.tapan.eventticketingengine.ticket.service;

import com.tapan.eventticketingengine.event.entity.Event;
import com.tapan.eventticketingengine.event.enums.EventStatus;
import com.tapan.eventticketingengine.event.exception.EventNotDraftException;
import com.tapan.eventticketingengine.event.exception.EventNotFoundException;
import com.tapan.eventticketingengine.event.repository.EventRepository;
import com.tapan.eventticketingengine.ticket.dto.CreateTicketTypeRequest;
import com.tapan.eventticketingengine.ticket.dto.TicketTypeAvailabilityResponse;
import com.tapan.eventticketingengine.ticket.dto.TicketTypeResponse;
import com.tapan.eventticketingengine.ticket.entity.TicketType;
import com.tapan.eventticketingengine.ticket.exception.DuplicateTicketTypeException;
import com.tapan.eventticketingengine.ticket.exception.TicketTypeNotFoundException;
import com.tapan.eventticketingengine.ticket.repository.TicketTypeRepository;
import com.tapan.eventticketingengine.user.entity.User;
import com.tapan.eventticketingengine.user.enums.UserRole;
import com.tapan.eventticketingengine.user.repository.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class TicketTypeService {

    private static final String CATEGORY_NAME_CONSTRAINT =
            "uk_ticket_types_event_name";

    private final TicketTypeRepository ticketTypeRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public TicketTypeService(
            TicketTypeRepository ticketTypeRepository,
            EventRepository eventRepository,
            UserRepository userRepository
    ) {
        this.ticketTypeRepository = ticketTypeRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TicketTypeResponse createTicketType(
            Long eventId,
            CreateTicketTypeRequest request,
            String organizerEmail
    ) {

        User organizer = getOrganizer(organizerEmail);

        Event event = eventRepository
                .findByIdAndOrganizer_Id(eventId, organizer.getId())
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new EventNotDraftException(eventId);
        }

        TicketType ticketType = new TicketType(
                event,
                request.name(),
                request.price(),
                request.totalQuantity()
        );

        if (ticketTypeRepository.existsByEvent_IdAndName(
                eventId,
                ticketType.getName()
        )) {
            throw new DuplicateTicketTypeException(ticketType.getName());
        }

        try {
            TicketType savedTicketType =
                    ticketTypeRepository.saveAndFlush(ticketType);

            return toResponse(savedTicketType);

        } catch (DataIntegrityViolationException exception) {

            if (isDuplicateCategoryNameConstraint(exception)) {
                throw new DuplicateTicketTypeException(
                        ticketType.getName()
                );
            }

            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public List<TicketTypeResponse> getTicketTypesByEvent(Long eventId) {

        eventRepository.findByIdAndStatus(eventId, EventStatus.PUBLISHED)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        return ticketTypeRepository
                .findAllByEvent_IdOrderByNameAsc(eventId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TicketTypeResponse getTicketType(Long ticketTypeId) {

        TicketType ticketType = ticketTypeRepository
                .findByIdAndEvent_Status(
                        ticketTypeId,
                        EventStatus.PUBLISHED
                )
                .orElseThrow(() ->
                        new TicketTypeNotFoundException(ticketTypeId)
                );

        return toResponse(ticketType);
    }

    @Transactional(readOnly = true)
    public TicketTypeAvailabilityResponse getTicketTypeAvailability(
            Long ticketTypeId
    ) {

        TicketType ticketType = ticketTypeRepository
                .findByIdAndEvent_Status(
                        ticketTypeId,
                        EventStatus.PUBLISHED
                )
                .orElseThrow(() ->
                        new TicketTypeNotFoundException(ticketTypeId)
                );

        return new TicketTypeAvailabilityResponse(
                ticketType.getId(),
                ticketType.getName(),
                ticketType.getTotalQuantity(),
                ticketType.getAvailableQuantity()
        );
    }

    private User getOrganizer(String organizerEmail) {

        User organizer = userRepository.findByEmail(organizerEmail)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Authenticated organizer not found"
                        )
                );

        if (organizer.getRole() != UserRole.ORGANIZER) {
            throw new IllegalStateException(
                    "Authenticated user is not an organizer"
            );
        }

        return organizer;
    }

    private TicketTypeResponse toResponse(TicketType ticketType) {

        return new TicketTypeResponse(
                ticketType.getId(),
                ticketType.getEvent().getId(),
                ticketType.getName(),
                ticketType.getPrice(),
                ticketType.getTotalQuantity(),
                ticketType.getAvailableQuantity(),
                ticketType.getReservedQuantity(),
                ticketType.getSoldQuantity(),
                ticketType.getCreatedAt(),
                ticketType.getUpdatedAt()
        );
    }

    private boolean isDuplicateCategoryNameConstraint(
            DataIntegrityViolationException exception
    ) {

        Throwable cause = exception;

        while (cause != null) {

            if (cause instanceof ConstraintViolationException violationException) {

                String constraintName = violationException.getConstraintName();

                return constraintName != null
                        && constraintName.endsWith(CATEGORY_NAME_CONSTRAINT);
            }

            cause = cause.getCause();
        }

        return false;
    }
}
