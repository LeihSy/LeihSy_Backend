package com.hse.leihsy.model.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

/**
 * User Entity - Benutzer des Systems.
 * unique_id kommt von Keycloak, alles andere wird lokal gespeichert.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
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


    @Column(name = "email")
    private String email;

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
    @OneToMany(mappedBy = "lender", fetch = FetchType.LAZY)
    private List<Booking> bookingsAsLender = new ArrayList<>();

    /**
     * Items die dieser User verleiht
     */
    @OneToMany(mappedBy = "lender", fetch = FetchType.LAZY)
    private List<Item> lendingItems = new ArrayList<>();

    // Constructors

    public User(String uniqueId, String name) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.budget = BigDecimal.ZERO;
    }

}