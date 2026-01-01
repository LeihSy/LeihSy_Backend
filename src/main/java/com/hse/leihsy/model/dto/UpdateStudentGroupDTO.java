package com.hse.leihsy.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO für das Aktualisieren einer StudentGroup
 * Alle Felder sind optional - nur gesetzte Felder werden aktualisiert
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Anfrage zum Aktualisieren einer Studentengruppe")
public class UpdateStudentGroupDTO {

    @Size(min = 2, max = 255, message = "Gruppenname muss zwischen 2 und 255 Zeichen lang sein")
    @Schema(description = "Neuer Name der Gruppe (optional)", example = "Projektgruppe VR-Film 2.0")
    private String name;

    @Size(max = 1000, message = "Beschreibung darf maximal 1000 Zeichen lang sein")
    @Schema(description = "Neue Beschreibung der Gruppe (optional)", example = "Aktualisierte Beschreibung")
    private String description;

    @DecimalMin(value = "0.0", message = "Budget muss mindestens 0 sein")
    @Schema(description = "Budget der Gruppe (optional)", example = "5000.00")
    private BigDecimal budget;
}