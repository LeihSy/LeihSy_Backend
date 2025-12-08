package com.hse.leihsy.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
/**
 * ProductSet Entity - Empfehlungssystem für zusammengehörige Produkte
 *
 * Zeigt welche Products oft zusammen ausgeliehen werden.
 * NICHT bidirektional: Eintrag muss pro Richtung angelegt werden falls gewünscht.
 *
 * Beispiel:
 * - Meta Quest 3 (id=1) empfiehlt Controller-Ladestation (id=5)
 * - Kamera Sony A7 empfiehlt SD-Karte und Stativ
 */
@Entity
@Table(name = "sets")
@IdClass(ProductSetId.class)
@Getter
@Setter
@NoArgsConstructor
public class ProductSet {

    /**
     * Hauptprodukt (das Produkt das man ansieht)
     */
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_product", nullable = false)
    private Product parentProduct;

    /**
     * Empfohlenes Produkt (das dazu empfohlen wird)
     */
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_product_child", nullable = false)
    private Product childProduct;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Lifecycle Callbacks

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    public ProductSet(Product parentProduct, Product childProduct) {
        this.parentProduct = parentProduct;
        this.childProduct = childProduct;
    }
}