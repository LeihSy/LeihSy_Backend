package com.hse.leihsy.model.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Category Entity - Oberkategorie für Products
 * Beispiele: VR-Equipment, Foto-Equipment, Audio, IT-Geräte
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
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

    public Category(String name) {
        this.name = name;
    }
}