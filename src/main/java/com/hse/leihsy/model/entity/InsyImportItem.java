package com.hse.leihsy.model.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * InsyImportItem Entity - Staging-Bereich fuer InSy-Importe.
 *
 * Daten von InSy landen hier und werden vom Admin reviewed.
 * Nach dem Import wird eine Referenz zum erstellten Product/Item gespeichert.
 */
@Entity
@Table(name = "insy_import_items")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InsyImportItem extends BaseEntity {

    /**
     * ID aus dem InSy-System (eindeutig pro InSy-Eintrag)
     */
    @Column(name = "insy_id", nullable = false, unique = true)
    private Long insyId;

    /**
     * Name des Gegenstands von InSy
     */
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /**
     * Beschreibung von InSy (optional)
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Standort/Raum von InSy als String (z.B. "F01.402")
     * Wird nicht direkt auf Location gemappt, da InSy-Locations
     * nicht unseren entsprechen muessen
     */
    @Column(name = "location", length = 255)
    private String location;

    /**
     * Besitzer von InSy (optional)
     */
    @Column(name = "owner", length = 255)
    private String owner;

    /**
     * Inventarnummer von InSy (optional, kann beim Import gesetzt werden)
     */
    @Column(name = "inv_number", length = 255)
    private String invNumber;

    /**
     * Import-Status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private InsyImportStatus status = InsyImportStatus.PENDING;

    /**
     * Referenz zum erstellten Product (nach Import als neues Product)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imported_product_id")
    private Product importedProduct;

    /**
     * Referenz zum erstellten/aktualisierten Item (nach Import)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imported_item_id")
    private Item importedItem;

    /**
     * Notiz vom Admin (z.B. Ablehnungsgrund)
     */
    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;

    // Helper Methods

    /**
     * Prueft ob dieser Eintrag noch importiert werden kann
     */
    public boolean canBeImported() {
        return status == InsyImportStatus.PENDING;
    }

    /**
     * Markiert den Eintrag als importiert mit Referenz zum erstellten Product
     */
    public void markAsImportedWithProduct(Product product, Item item) {
        this.status = InsyImportStatus.IMPORTED;
        this.importedProduct = product;
        this.importedItem = item;
    }

    /**
     * Markiert den Eintrag als importiert zu bestehendem Product
     */
    public void markAsImportedToExistingProduct(Item item) {
        this.status = InsyImportStatus.IMPORTED;
        this.importedItem = item;
    }

    /**
     * Markiert den Eintrag als Update eines bestehenden Items
     */
    public void markAsUpdated(Item item) {
        this.status = InsyImportStatus.UPDATED;
        this.importedItem = item;
    }

    /**
     * Markiert den Eintrag als abgelehnt
     */
    public void markAsRejected(String reason) {
        this.status = InsyImportStatus.REJECTED;
        this.adminNote = reason;
    }
}