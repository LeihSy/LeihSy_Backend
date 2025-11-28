package com.hse.leihsy.model.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Product Entity - Modell/Typ eines Gegenstandes.
 * Beispiel: "Meta Quest 3" (davon gibt es dann mehrere Items)
 *
 * 3-Ebenen-Modell:
 * Categories (Oberkategorie: "VR-Brillen")
 *     -> Products (Modell: "Meta Quest 3")
 *         -> Items (Exemplar: Meta Quest 3 #12345)
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Product extends BaseEntity {

    /**
     * ID aus InSy (Inventarsystem)
     */
    @Column(name = "insy_id")
    private Long insyId;

    /**
     * Produktname (z.B. "Meta Quest 3", "Sony A7 III")
     */
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /**
     * Maximale Ausleihdauer in Tagen
     */
    @Column(name = "expiry_date")
    private Integer expiryDate;

    /**
     * Beschreibung des Produkts
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Preis/Tagessatz
     */
    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * URL zum Produktbild
     */
    @Column(name = "image_url", length = 255)
    private String imageUrl;

    /**
     * Zubehör als JSON-String
     * Format: ["Controller", "Ladekabel", "Tragetasche"]
     */
    @Column(name = "accessories", columnDefinition = "TEXT")
    private String accessories;

    // Relationships

    /**
     * Kategorie zu der dieses Produkt gehört
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    /**
     * Standort wo das Produkt gelagert/ausgegeben wird
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    /**
     * Verleiher (Person die Ausgabe/Rücknahme macht)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lender_id")
    private User lender;

    /**
     * Physische Exemplare dieses Produkts
     */
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Item> items = new ArrayList<>();

    /**
     * Empfohlene Sets (andere Products die oft zusammen ausgeliehen werden)
     */
    @OneToMany(mappedBy = "parentProduct", fetch = FetchType.LAZY)
    private List<ProductSet> recommendedSets = new ArrayList<>();

    // Constructor
    public Product(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Helper Methods
    /**
     * Zählt verfügbare Items dieses Produkts
     */
    public long getAvailableItemCount() {
        return items.stream()
                .filter(item -> item.getDeletedAt() == null)
                .filter(Item::isAvailable)
                .count();
    }

    /**
     * Gesamtzahl der Items (ohne gelöschte)
     */
    public long getTotalItemCount() {
        return items.stream()
                .filter(item -> item.getDeletedAt() == null)
                .count();
    }
}