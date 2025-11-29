package com.hse.leihsy.model.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * User Entity - Benutzer des Systems.
 * unique_id kommt von Keycloak, alles andere wird lokal gespeichert.
 */
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    /**
     * Eindeutige ID von Keycloak (sub claim aus JWT Token)
     */
    @Column(name = "unique_id", unique = true)
    private String uniqueId;

    /**
     * Name des Benutzers aus Keycloak
     */
    @Column(name = "name")
    private String name;

    /**
     * Budget für Ausleihen
     */
    @Column(name = "budget", precision = 10, scale = 2)
    private BigDecimal budget;

    // Relationships

    /**
     * Ausleihen als Entleiher (Student)
     */
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Booking> bookingsAsUser = new ArrayList<>();

    /**
     * Ausleihen als Verleiher
     */
    @OneToMany(mappedBy = "receiver", fetch = FetchType.LAZY)
    private List<Booking> bookingsAsLender = new ArrayList<>();

    /**
     * Items die dieser User verleiht
     */
    @OneToMany(mappedBy = "lender", fetch = FetchType.LAZY)
    private List<Item> lendingItems = new ArrayList<>();

    // Constructors

    public User() {
    }

    public User(String uniqueId, String name) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.budget = BigDecimal.ZERO;
    }

    // Getters and Setters

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public List<Booking> getBookingsAsUser() {
        return bookingsAsUser;
    }

    public void setBookingsAsUser(List<Booking> bookingsAsUser) {
        this.bookingsAsUser = bookingsAsUser;
    }

    public List<Booking> getbookingsAsLender() {
        return bookingsAsLender;
    }

    public void setbookingsAsLender(List<Booking> bookingsAsLender) {
        this.bookingsAsLender = bookingsAsLender;
    }

    public List<Product> getLendingProducts() {
        return lendingProducts;
    }

    public void setLendingProducts(List<Product> lendingProducts) {
        this.lendingProducts = lendingProducts;
    }
}