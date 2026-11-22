package com.tapan.eventticketingengine.ticket.entity;

import com.tapan.eventticketingengine.common.entity.BaseAuditableEntity;
import com.tapan.eventticketingengine.event.entity.Event;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

@Entity
@Table(
        name = "ticket_types",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ticket_types_event_name",
                        columnNames = {"event_id", "name"}
                )
        },
        check = {
                @CheckConstraint(
                        name = "ck_ticket_types_price",
                        constraint = "price >= 0"
                ),
                @CheckConstraint(
                        name = "ck_ticket_types_total_quantity",
                        constraint = "total_quantity > 0"
                ),
                @CheckConstraint(
                        name = "ck_ticket_types_available_quantity",
                        constraint = "available_quantity >= 0"
                ),
                @CheckConstraint(
                        name = "ck_ticket_types_reserved_quantity",
                        constraint = "reserved_quantity >= 0"
                ),
                @CheckConstraint(
                        name = "ck_ticket_types_sold_quantity",
                        constraint = "sold_quantity >= 0"
                ),
                @CheckConstraint(
                        name = "ck_ticket_types_quantity_balance",
                        constraint = "total_quantity = available_quantity + reserved_quantity + sold_quantity"
                )
        }
)
public class TicketType extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "total_quantity", nullable = false)
    private int totalQuantity;

    @Column(name = "available_quantity", nullable = false)
    private int availableQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity;

    @Column(name = "sold_quantity", nullable = false)
    private int soldQuantity;

    protected TicketType() {
    }

    public TicketType(
            Event event,
            String name,
            BigDecimal price,
            int totalQuantity
    ) {
        if (event == null) {
            throw new IllegalArgumentException(
                    "Ticket category must belong to an event"
            );
        }

        if (totalQuantity <= 0) {
            throw new IllegalArgumentException(
                    "Total ticket quantity must be greater than zero"
            );
        }

        this.event = event;
        this.name = normalizeName(name);
        this.price = validatePrice(price);

        this.totalQuantity = totalQuantity;
        this.availableQuantity = totalQuantity;
        this.reservedQuantity = 0;
        this.soldQuantity = 0;
    }

    private static String normalizeName(String name) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Ticket category name is required"
            );
        }

        String normalizedName = name.strip().toUpperCase(Locale.ROOT);

        if (normalizedName.length() > 100) {
            throw new IllegalArgumentException(
                    "Ticket category name cannot exceed 100 characters"
            );
        }

        return normalizedName;
    }

    private static BigDecimal validatePrice(BigDecimal price) {

        if (price == null) {
            throw new IllegalArgumentException(
                    "Ticket category price is required"
            );
        }

        if (price.signum() < 0) {
            throw new IllegalArgumentException(
                    "Ticket category price cannot be negative"
            );
        }

        BigDecimal normalizedPrice;

        try {
            normalizedPrice = price.setScale(
                    2,
                    RoundingMode.UNNECESSARY
            );
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(
                    "Ticket category price cannot have more than two decimal places",
                    exception
            );
        }

        if (normalizedPrice.precision() - normalizedPrice.scale() > 8) {
            throw new IllegalArgumentException(
                    "Ticket category price cannot exceed 99999999.99"
            );
        }

        return normalizedPrice;
    }

    public Long getId() {
        return id;
    }

    public Event getEvent() {
        return event;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public int getReservedQuantity() {
        return reservedQuantity;
    }

    public int getSoldQuantity() {
        return soldQuantity;
    }
}
