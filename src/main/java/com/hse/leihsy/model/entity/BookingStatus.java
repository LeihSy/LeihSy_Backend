package com.hse.leihsy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
@Getter
@AllArgsConstructor
/**
 * Booking Status Enum
 *
 * Der Status wird berechnet aus den Timestamp-Feldern der Booking Entity.
 *
 * Status-Übergang:
 * PENDING -> (Verleiher bestätigt) -> CONFIRMED -> (Abholung) -> PICKED_UP -> (Rückgabe) -> RETURNED
 *
 * Alternative Wege:
 * PENDING -> (24h ohne Bestätigung) -> CANCELLED
 * PENDING -> (Verleiher lehnt ab) -> REJECTED
 * CONFIRMED -> (24h ohne Abholung) -> EXPIRED
 */
public enum BookingStatus {

    /**
     * Anfrage erstellt, wartet auf Bestätigung durch Verleiher
     */
    PENDING("Ausstehend"),

    /**
     * Verleiher hat bestätigt, wartet auf Abholung
     */
    CONFIRMED("Bestätigt"),

    /**
     * Item wurde abgeholt/ausgegeben
     */
    PICKED_UP("Ausgeliehen"),

    /**
     * Item wurde zurückgegeben
     */
    RETURNED("Zurückgegeben"),

    /**
     * Anfrage wurde vom Verleiher abgelehnt
     */
    REJECTED("Abgelehnt"),

    /**
     * Bestätigt aber nicht abgeholt (24h nach confirmed_pickup)
     */
    EXPIRED("Abgelaufen"),

    /**
     * Automatisch storniert (24h ohne Bestätigung)
     */
    CANCELLED("Storniert");

    private final String displayName;

    /**
     * Prüft ob Buchung "aktiv" ist (Item ist beim Entleiher)
     */
    public boolean isActive() {
        return this == CONFIRMED || this == PICKED_UP;
    }

    /**
     * Prüft ob Buchung abgeschlossen ist
     */
    public boolean isCompleted() {
        return this == RETURNED;
    }

    /**
     * Prüft ob Buchung fehlgeschlagen/abgebrochen ist
     */
    public boolean isFailed() {
        return this == REJECTED || this == EXPIRED || this == CANCELLED;
    }
}