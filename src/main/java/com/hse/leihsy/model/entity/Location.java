package com.hse.leihsy.model.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Location Entity - Lagerorte/Räme für Products.
 * Beispiele: "F01.402", "Bibliothek Flandernstrasse"
 */
@Entity
@Table(name = "locations")
@Getter
@Setter
@NoArgsConstructor
public class Location extends BaseEntity {

    /**
     * Raumnummer oder Ortsbezeichnung
     */
    @Column(name = "room_nr", nullable = false, length = 255)
    private String roomNr;

    // Relationships

    /**
     * Products an diesem Standort
     */
    @OneToMany(mappedBy = "location", fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();

    public Location(String roomNr) {
        this.roomNr = roomNr;
    }
}