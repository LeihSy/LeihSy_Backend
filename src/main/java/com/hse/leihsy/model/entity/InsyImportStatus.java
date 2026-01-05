package com.hse.leihsy.model.entity;

/**
 * Status eines InSy-Import-Eintrags.
 *
 * PENDING: Neu von InSy empfangen, wartet auf Admin-Review
 * IMPORTED: Erfolgreich als Product/Item importiert
 * REJECTED: Vom Admin abgelehnt
 * UPDATED: Existierendes Item wurde aktualisiert
 */
public enum InsyImportStatus {
    PENDING,
    IMPORTED,
    REJECTED,
    UPDATED;

    /**
     * Prueft ob der Eintrag noch bearbeitet werden kann
     */
    public boolean isPending() {
        return this == PENDING;
    }

    /**
     * Prueft ob der Import abgeschlossen ist
     */
    public boolean isProcessed() {
        return this == IMPORTED || this == REJECTED || this == UPDATED;
    }
}