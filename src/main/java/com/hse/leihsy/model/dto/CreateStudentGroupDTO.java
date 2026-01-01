package com.hse.leihsy.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO für das Erstellen einer neuen StudentGroup
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Anfrage zum Erstellen einer neuen Studentengruppe")
public class CreateStudentGroupDTO {

    @NotBlank(message = "Gruppenname ist erforderlich")
    @Size(min = 2, max = 255, message = "Gruppenname muss zwischen 2 und 255 Zeichen lang sein")
    @Schema(description = "Name der Gruppe", example = "Projektgruppe VR-Film")
    private String name;

    @Size(max = 1000, message = "Beschreibung darf maximal 1000 Zeichen lang sein")
    @Schema(description = "Optionale Beschreibung der Gruppe", example = "Gruppe für das VR-Filmprojekt im WS 2025")
    private String description;

    @DecimalMin(value = "0.0", message = "Budget muss mindestens 0 sein")
    @Schema(description = "Initiales Budget der Gruppe (optional, Standard: 0)", example = "1000.00")
    private BigDecimal budget;

    @Schema(description = "Liste von User-IDs die als Mitglieder hinzugefügt werden sollen (optional)", example = "[2, 3, 5]")
    private List<Long> memberIds;
}