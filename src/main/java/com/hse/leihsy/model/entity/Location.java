package com.hse.leihsy.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

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
    @JsonIgnore
    @OneToMany(mappedBy = "location", fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();

    // Constructors

    public Location(String roomNr) {
        this.roomNr = roomNr;
    }
}