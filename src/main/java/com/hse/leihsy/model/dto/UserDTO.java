package com.hse.leihsy.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO fuer User-Daten inkl. Keycloak-Rollen
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    @Builder.Default
    @Schema(description = "Rollen aus Keycloak", example = "[\"ROLE_STUDENT\", \"ROLE_LENDER\"]")
    private List<String> roles = new ArrayList<>();

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