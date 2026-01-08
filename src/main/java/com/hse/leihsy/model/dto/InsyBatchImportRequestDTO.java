package com.hse.leihsy.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

/**
 * DTO fuer Batch-Import mehrerer InSy-Eintraege zu einem Product.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for batch importing multiple InSy items to one product")
public class InsyBatchImportRequestDTO {

    @NotEmpty(message = "Mindestens eine Import-Item-ID ist erforderlich")
    @Schema(description = "List of InsyImportItem IDs to import", example = "[1, 2, 3]", required = true)
    private List<Long> importItemIds;

    @NotNull(message = "Product-ID ist erforderlich")
    @Schema(description = "ID of the product to add all items to", example = "5", required = true)
    private Long productId;

    @Schema(description = "Lender ID to assign to all new items", example = "7")
    private Long lenderId;

    @Schema(description = "Prefix for inventory numbers (will be auto-numbered)", example = "VR")
    private String invNumberPrefix;
}