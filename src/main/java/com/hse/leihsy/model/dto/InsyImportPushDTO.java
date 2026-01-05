package com.hse.leihsy.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO fuer eingehende Daten von InSy.
 * Wird vom Mock-Endpoint und spaeter vom echten InSy-Sync verwendet.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for pushing item data from InSy")
public class InsyImportPushDTO {

    @NotNull(message = "InSy-ID ist erforderlich")
    @Schema(description = "ID from InSy system", example = "12345", required = true)
    private Long insyId;

    @NotBlank(message = "Name ist erforderlich")
    @Schema(description = "Name of the item", example = "Meta Quest 3", required = true)
    private String name;

    @Schema(description = "Description of the item", example = "VR-Brille mit Touch-Controllern")
    private String description;

    @Schema(description = "Location/room", example = "F01.402")
    private String location;

    @Schema(description = "Owner of the item", example = "Christian Haas")
    private String owner;

    @Schema(description = "Inventory number", example = "VR-001")
    private String invNumber;
}