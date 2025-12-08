package com.hse.leihsy.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Booking Entity - Ausleih-Anfrage und Buchung
 * Bildet den kompletten Lebenszyklus einer Ausleihe ab:
 * PENDING -> CONFIRMED -> PICKED_UP -> RETURNED
 * Status wird berechnet aus den Timestamp-Feldern
 */
@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
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
     * Gewüschter Ausleihbeginn
     */
    @Column(name = "start_date")
    private LocalDateTime startDate;

    /**
     * Gewünschtes Ausleihende
     */
    @Column(name = "end_date")
    private LocalDateTime endDate;

    /**
     * Aktueller Abholtermin-Vorschlag
     */
    @Column(name = "proposal_pickup")
    private LocalDateTime proposalPickup;

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
     * Verleiher (empfängt die Anfrage, wird aus Item.product.lender ermittelt)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id_receiver")
    private User receiver;

    /**
     * Das ausgeliehene Item (physisches Exemplar)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    // Constructor
    public Booking(User user, Item item, LocalDateTime startDate, LocalDateTime endDate) {
        this.user = user;
        this.item = item;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = BookingStatus.PENDING.name();

        // Verleiher automatisch aus Product setzen
        if (item != null && item.getProduct() != null && item.getProduct().getLender() != null) {
            this.receiver = item.getProduct().getLender();
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
}