package com.hse.leihsy.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * StudentGroup Entity - Gruppe von Studenten die gemeinsam ausleihen
 *
 * Ermoeglicht es Studierenden, sich zu Gruppen zusammenzuschließen und
 * gemeinsam Gegenstaende auszuleihen.
 */
@Entity
@Table(name = "student_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentGroup extends BaseEntity {

    /**
     * Name der Gruppe (z.B. "Projektgruppe VR-Film", "Team Alpha")
     */
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /**
     * Optionale Beschreibung der Gruppe
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Ersteller der Gruppe (automatisch Owner/Admin der Gruppe)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    /**
     * Budget der Gruppe (für spaetere Implementierung)
     * NULL = kein Budget-Limit
     */
    @Column(name = "budget", precision = 10, scale = 2)
    private BigDecimal budget;

    /**
     * Mitglieder der Gruppe (M:N Beziehung)
     * Der Ersteller ist automatisch auch Mitglied
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "student_group_members",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> members = new HashSet<>();

    /**
     * Bookings die dieser Gruppe zugeordnet sind
     */
    @OneToMany(mappedBy = "studentGroup", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Booking> bookings = new HashSet<>();

    // ========================================
    // HELPER METHODEN
    // ========================================

    /**
     * Fuegt ein Mitglied zur Gruppe hinzu
     */
    public void addMember(User user) {
        members.add(user);
    }

    /**
     * Entfernt ein Mitglied aus der Gruppe
     * Der Ersteller kann nicht entfernt werden
     */
    public boolean removeMember(User user) {
        if (user.equals(createdBy)) {
            return false; // Ersteller kann nicht entfernt werden
        }
        return members.remove(user);
    }

    /**
     * Pruft ob ein User Mitglied der Gruppe ist
     */
    public boolean isMember(User user) {
        return members.contains(user);
    }

    /**
     * Prueft ob ein User der Ersteller (Owner) der Gruppe ist
     */
    public boolean isOwner(User user) {
        return createdBy != null && createdBy.equals(user);
    }

    /**
     * Gibt die Anzahl der Mitglieder zurück
     */
    public int getMemberCount() {
        return members.size();
    }
}