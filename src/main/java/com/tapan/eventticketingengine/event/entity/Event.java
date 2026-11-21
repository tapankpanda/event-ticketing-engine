package com.tapan.eventticketingengine.event.entity;

import com.tapan.eventticketingengine.common.entity.BaseAuditableEntity;
import com.tapan.eventticketingengine.event.enums.EventStatus;
import com.tapan.eventticketingengine.user.entity.User;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "events")
public class Event extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "venue", nullable = false, length = 255)
    private String venue;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EventStatus status = EventStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    protected Event() {
    }

    public Event(
            String title,
            String description,
            String venue,
            Instant startAt,
            Instant endAt,
            User organizer
    ) {
        this.title = title;
        this.description = description;
        this.venue = venue;
        this.startAt = startAt;
        this.endAt = endAt;
        this.organizer = organizer;
        this.status = EventStatus.DRAFT;
    }

    public void updateDetails(
            String title,
            String description,
            String venue,
            Instant startAt,
            Instant endAt
    ) {
        this.title = title;
        this.description = description;
        this.venue = venue;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public void publish() {
        this.status = EventStatus.PUBLISHED;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getVenue() {
        return venue;
    }

    public Instant getStartAt() {
        return startAt;
    }

    public Instant getEndAt() {
        return endAt;
    }

    public EventStatus getStatus() {
        return status;
    }

    public User getOrganizer() {
        return organizer;
    }
}
