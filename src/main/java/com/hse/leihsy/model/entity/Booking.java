package com.hse.leihsy.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Booking Entity - Ausleih-Anfrage und Buchung
 * Bildet den kompletten Lebenszyklus einer Ausleihe ab:
 * PENDING -> CONFIRMED -> PICKED_UP -> RETURNED
 *
 * Status wird berechnet aus den Timestamp-Feldern
 */
@Entity
@Table(name = "bookings")
public class Booking extends BaseEntity {

    /**
     * Optionale Nachricht vom Entleiher
     */
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    /**
     * Status-String (wird berechnet, aber auch gespeichert für einfache Queries)
     */
    @Column(name = "status", length = 255)
    private String status;

    /**
     * Gewünschter Ausleihbeginn
     */
    @Column(name = "start_date")
    private LocalDateTime startDate;

    /**
     * Gewünschtes Ausleihende
     */
    @Column(name = "end_date")
    private LocalDateTime endDate;

    /**
     * Terminvorschläge als JSON-Array
     * Format: ["2025-12-01T10:00:00", "2025-12-01T14:00:00", "2025-12-02T09:00:00"]
     */
    @Column(name = "proposed_pickups", columnDefinition = "TEXT")
    private String proposedPickups;

    /**
     * Wer hat zuletzt einen Termin vorgeschlagen (User-ID)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_by")
    private User proposalBy;

    /**
     * Finaler bestätigter Abholtermin
     */
    @Column(name = "confirmed_pickup")
    private LocalDateTime confirmedPickup;

    /**
     * Tatsächliche Ausgabe (wann wurde das Item übergeben)
     */
    @Column(name = "distribution_date")
    private LocalDateTime distributionDate;

    /**
     * Tatsächliche Rückgabe
     */
    @Column(name = "return_date")
    private LocalDateTime returnDate;

    // Relationships

    /**
     * Entleiher (Student der ausleiht)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Verleiher (wird aus Item.lender ermittelt)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lender_id")
    private User lender;

    /**
     * Das ausgeliehene Item (physisches Exemplar)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    // Constructors

    public Booking() {
    }

    public Booking(User user, Item item, LocalDateTime startDate, LocalDateTime endDate) {
        this.user = user;
        this.item = item;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = BookingStatus.PENDING.name();

        // Verleiher automatisch aus Item setzen
        if (item != null && item.getLender() != null) {
            this.lender = item.getLender();
        }
    }

    /**
     * Berechnet den aktuellen Status basierend auf den Timestamp-Feldern.
     */
    public BookingStatus calculateStatus() {
        // Gelöscht/Abgelehnt
        if (getDeletedAt() != null) {
            return BookingStatus.REJECTED;
        }

        // Zurückgegeben
        if (returnDate != null) {
            return BookingStatus.RETURNED;
        }

        // Ausgegeben (aber noch nicht zurück)
        if (distributionDate != null) {
            return BookingStatus.PICKED_UP;
        }

        // Bestätigt (aber noch nicht abgeholt)
        if (confirmedPickup != null) {
            // Prüfen ob abgelaufen (24h nach confirmed_pickup)
            if (LocalDateTime.now().isAfter(confirmedPickup.plusHours(24))) {
                return BookingStatus.EXPIRED;
            }
            return BookingStatus.CONFIRMED;
        }

        // Noch nicht bestätigt - prüfen ob automatisch storniert
        if (getCreatedAt() != null && LocalDateTime.now().isAfter(getCreatedAt().plusHours(24))) {
            return BookingStatus.CANCELLED;
        }

        return BookingStatus.PENDING;
    }

    /**
     * Aktualisiert das status-Feld basierend auf Berechnung
     */
    public void updateStatus() {
        this.status = calculateStatus().name();
    }

    // Getters and Setters

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public String getProposedPickups() {
        return proposedPickups;
    }

    public void setProposedPickups(String proposedPickups) {
        this.proposedPickups = proposedPickups;
    }

    public User getProposalBy() {
        return proposalBy;
    }

    public void setProposalBy(User proposalBy) {
        this.proposalBy = proposalBy;
    }

    public LocalDateTime getConfirmedPickup() {
        return confirmedPickup;
    }

    public void setConfirmedPickup(LocalDateTime confirmedPickup) {
        this.confirmedPickup = confirmedPickup;
    }

    public LocalDateTime getDistributionDate() {
        return distributionDate;
    }

    public void setDistributionDate(LocalDateTime distributionDate) {
        this.distributionDate = distributionDate;
    }

    public LocalDateTime getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDateTime returnDate) {
        this.returnDate = returnDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getLender() {
        return lender;
    }

    public void setLender(User lender) {
        this.lender = lender;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }
}