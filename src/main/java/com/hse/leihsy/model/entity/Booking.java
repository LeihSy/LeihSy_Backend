package com.hse.leihsy.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

/**
 * Booking Entity - Ausleih-Anfrage und Buchung
 * Bildet den kompletten Lebenszyklus einer Ausleihe ab:
 * PENDING -> CONFIRMED -> PICKED_UP -> RETURNED
 *
 * Status wird berechnet aus den Timestamp-Feldern
 */
@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking extends BaseEntity {

    /**
     * Optionale Nachricht vom Entleiher
     */
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    /**
     * Status-String (wird berechnet, aber auch gespeichert fuer einfache Queries)
     */
    @Column(name = "status", length = 255)
    private String status;

    /**
     * Gewuenschter Ausleihbeginn
     */
    @Column(name = "start_date")
    private LocalDateTime startDate;

    /**
     * Gewuenschtes Ausleihende
     */
    @Column(name = "end_date")
    private LocalDateTime endDate;

    /**
     * Terminvorschlaege als JSON-Array
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
     * Finaler bestaetigter Abholtermin
     */
    @Column(name = "confirmed_pickup")
    private LocalDateTime confirmedPickup;

    /**
     * Sicherheits-Token zur Abholbestätigung
     */
    @Column(name = "pickup_token")
    private String pickupToken;

    /**
     * Ablaufzeitpunkt des Abhol-Tokens
     */
    @Column(name = "pickup_token_expiry")
    private LocalDateTime pickupTokenExpiry;

    /**
     * Tatsaechliche Ausgabe (wann wurde das Item uebergeben)
     */
    @Column(name = "distribution_date")
    private LocalDateTime distributionDate;

    /**
     * Tatsaechliche Rueckgabe
     */
    @Column(name = "return_date")
    private LocalDateTime returnDate;

    // ========================================
    // RELATIONSHIPS
    // ========================================

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


    /**
     * Optionale Zuordnung zu einer Studentengruppe
     * NULL = Einzelbuchung, sonst = Gruppenbuchung
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private StudentGroup studentGroup;

    /**
     * Konstruktor fuer neue Buchung mit automatischer Verleiher-Zuweisung
     */
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
        // Geloescht/Abgelehnt
        if (getDeletedAt() != null) {
            return BookingStatus.REJECTED;
        }

        // Zurueckgegeben
        if (returnDate != null) {
            return BookingStatus.RETURNED;
        }

        // Ausgegeben (aber noch nicht zurueck)
        if (distributionDate != null) {
            return BookingStatus.PICKED_UP;
        }

        // Bestaetigt (aber noch nicht abgeholt)
        if (confirmedPickup != null) {
            // Pruefen ob abgelaufen (24h nach confirmed_pickup)
            if (LocalDateTime.now().isAfter(confirmedPickup.plusHours(24))) {
                return BookingStatus.EXPIRED;
            }
            return BookingStatus.CONFIRMED;
        }

        // Noch nicht bestaetigt - pruefen ob automatisch storniert
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

    /**
     * Prueft ob diese Buchung zu einer Gruppe gehoert
     */
    public boolean isGroupBooking() {
        return studentGroup != null;
    }
}