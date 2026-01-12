package com.hse.leihsy.model.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;


/**
 * Item Entity - Physisches Exemplar eines Products.
 * Beispiel: Meta Quest 3 mit Inventarnummer VR-001
 *
 * Items sind die konkreten Geräte die ausgeliehen werden.
 */
@Entity
@Table(name = "items")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)

public class Item extends BaseEntity {

    /**
     * ID aus InSy
     */
    @Column(name = "insy_id")
    private Long insyId;

    /**
     * Besitzer des Gegenstands (z.B. Christian Haas)
     */
    @Column(name = "owner", length = 255)
    private String owner;

    /**
     * Inventarnummer (eindeutig, z.B. "VR-001", "CAM-042")
     */
    @Column(name = "invnumber", length = 255, unique = true)
    private String invNumber;

    // Relationships

    /**
     * Produkt-Modell zu dem dieses Item gehört
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * Verleiher - Person die Ausgabe/Rücknahme macht
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lender_id")
    private User lender;

    /**
     * Buchungen für dieses Item
     */
    @Builder.Default
    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY)
    private List<Booking> bookings = new ArrayList<>();

    // Helper Methods

    /**
     * Prüft ob dieses Item aktuell verfügbar ist.
     * Verfügbar = keine aktive Buchung (PENDING, CONFIRMED, oder PICKED_UP)
     */
    public boolean isAvailable() {
        return bookings.stream()
                .filter(b -> b.getDeletedAt() == null)
                .noneMatch(b -> {
                    boolean hasReturnDate = b.getReturnDate() != null;
                    return !hasReturnDate;
                });
    }
    /**
     * Verwandte Items (Zubehör/Empfehlungen)
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "item_relations",
            joinColumns = @JoinColumn(name = "item_id"),
            inverseJoinColumns = @JoinColumn(name = "related_item_id")
    )
    @Builder.Default
    private List<Item> relatedItems = new ArrayList<>();

    /**
     * Prüft Verfügbarkeit für einen bestimmten Zeitraum
     */
    public boolean isAvailableForPeriod(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        return bookings.stream()
                .filter(b -> b.getDeletedAt() == null)
                .filter(b -> b.getReturnDate() == null)
                .noneMatch(b -> {
                    java.time.LocalDateTime bookingStart = b.getStartDate();
                    java.time.LocalDateTime bookingEnd = b.getEndDate();

                    if (bookingStart == null || bookingEnd == null) {
                        return false;
                    }

                    return !startDate.isAfter(bookingEnd) && !endDate.isBefore(bookingStart);
                });
    }
}