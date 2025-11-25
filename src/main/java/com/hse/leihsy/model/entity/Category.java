package com.hse.leihsy.model.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Category Entity - Oberkategorie für Products
 * Beispiele: VR-Equipment, Foto-Equipment, Audio, IT-Geräte
 */
@Entity
@Table(name = "categories")
public class Category extends BaseEntity {

    /**
     * Name der Kategorie (z.B. "VR-Equipment", "Kameras")
     */
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    // Relationships

    /**
     * Products in dieser Kategorie
     */
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();

    // Constructors

    public Category() {
    }

    public Category(String name) {
        this.name = name;
    }

    // Getters and Setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}