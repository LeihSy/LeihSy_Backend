package com.hse.leihsy.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO fuer das Ablehnen eines Import-Eintrags.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for rejecting an InSy import item")
public class InsyRejectRequestDTO {

    @NotNull(message = "Import-Item-ID ist erforderlich")
    @Schema(description = "ID of the InsyImportItem to reject", example = "1", required = true)
    private Long importItemId;

    @Schema(description = "Reason for rejection", example = "Duplikat - Geraet existiert bereits unter anderer ID")
    private String reason;
}