package com.hse.leihsy.model.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Item Entity - Physisches Exemplar eines Products.
 * Beispiel: Meta Quest 3 mit Inventarnummer VR-001
 *
 * Items sind die konkreten Geräte die ausgeliehen werden.
 */
@Entity
@Table(name = "items")
public class Item extends BaseEntity {

    /**
     * ID aus InSy
     */
    @Column(name = "insy_id")
    private Long insyId;

    /**
     * Besitzer des Gegenstands
     * NICHT der Verleiher - der wird über Product.lender ermittelt
     */
    @Column(name = "owner", length = 255)
    private String owner;

    /**
     * Inventarnummer (eindeutig, z.B. "VR-001", "CAM-042")
     */
    @Column(name = "invnumber", length = 255, unique = true)
    private String invNumber;

    // =========================================    // Relationships

    /**
     * Produkt-Modell zu dem dieses Item gehört
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * Buchungen für dieses Item
     */
    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY)
    private List<Booking> bookings = new ArrayList<>();

    // Constructors

    public Item() {
    }

    public Item(String invNumber, Product product) {
        this.invNumber = invNumber;
        this.product = product;
    }

    public Item(String invNumber, String owner, Product product) {
        this.invNumber = invNumber;
        this.owner = owner;
        this.product = product;
    }

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
                    return !hasReturnDate; // Nicht zurückgegeben = nicht verfügbar
                });
    }

    /**
     * Prüft Verfügbarkeit fuer einen bestimmten Zeitraum
     */
    public boolean isAvailableForPeriod(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        return bookings.stream()
                .filter(b -> b.getDeletedAt() == null)
                .filter(b -> b.getReturnDate() == null) // Nur aktive Buchungen
                .noneMatch(b -> {
                    // Zeitraum-Überschneidung prüfen
                    java.time.LocalDateTime bookingStart = b.getStartDate();
                    java.time.LocalDateTime bookingEnd = b.getEndDate();

                    if (bookingStart == null || bookingEnd == null) {
                        return false;
                    }

                    // Überschneidung: startDate <= bookingEnd AND endDate >= bookingStart
                    return !startDate.isAfter(bookingEnd) && !endDate.isBefore(bookingStart);
                });
    }

    // Getters and Setters

    public Long getInsyId() {
        return insyId;
    }

    public void setInsyId(Long insyId) {
        this.insyId = insyId;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getInvNumber() {
        return invNumber;
    }

    public void setInvNumber(String invNumber) {
        this.invNumber = invNumber;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }
}