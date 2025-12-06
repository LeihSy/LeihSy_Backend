package com.hse.leihsy.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO fuer User-Daten inkl. Keycloak-Rollen
 */
@Schema(description = "Benutzer-Daten")
public class UserDTO {

    @Schema(description = "Datenbank-ID", example = "1")
    private Long id;

    @Schema(description = "Keycloak User-ID (sub claim)", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String uniqueId;

    @Schema(description = "Benutzername", example = "max.mustermann")
    private String name;

    @Schema(description = "Budget fuer Ausleihen", example = "100.00")
    private BigDecimal budget;

    @Schema(description = "Rollen aus Keycloak", example = "[\"ROLE_STUDENT\", \"ROLE_LENDER\"]")
    private List<String> roles = new ArrayList<>();

    // Constructors
    public UserDTO() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    // Helper Methoden fuer Rollen-Check
    public boolean isAdmin() {
        return roles.stream().anyMatch(r -> r.contains("ADMIN"));
    }

    public boolean isLender() {
        return roles.stream().anyMatch(r -> r.contains("LENDER"));
    }

    public boolean isStudent() {
        return roles.stream().anyMatch(r -> r.contains("STUDENT"));
    }
}