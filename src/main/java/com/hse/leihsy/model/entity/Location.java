package com.hse.leihsy.model.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Location Entity - Lagerorte/Räme für Products.
 * Beispiele: "F01.402", "Bibliothek Flandernstrasse"
 */
@Entity
@Table(name = "locations")
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

    // Constructors

    public Location() {
    }

    public Location(String roomNr) {
        this.roomNr = roomNr;
    }

    // Getters and Setters

    public String getRoomNr() {
        return roomNr;
    }

    public void setRoomNr(String roomNr) {
        this.roomNr = roomNr;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}