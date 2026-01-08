package com.hse.leihsy.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO fuer PATCH /api/insy/imports/{id}
 * Kombiniert Import und Reject in einem Request mit action-Parameter.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for updating import status (import or reject)")
public class InsyImportStatusUpdateDTO {

    @Schema(description = "Internal ID (set from path parameter)", hidden = true)
    private Long importItemId;

    @NotNull(message = "Action ist erforderlich")
    @Schema(description = "Action to perform: IMPORT or REJECT", example = "IMPORT", required = true)
    private Action action;

    // ========================================
    // Fields for IMPORT action
    // ========================================

    @Schema(description = "Type of import: NEW_PRODUCT or EXISTING_PRODUCT (required for IMPORT)",
            example = "EXISTING_PRODUCT")
    private InsyImportRequestDTO.ImportType importType;

    @Schema(description = "ID of existing product (required for EXISTING_PRODUCT)", example = "5")
    private Long existingProductId;

    @Schema(description = "Category ID for new product (required for NEW_PRODUCT)", example = "1")
    private Long categoryId;

    @Schema(description = "Location ID for new product (required for NEW_PRODUCT)", example = "2")
    private Long locationId;

    @Schema(description = "Price per day for new product", example = "5.00")
    private BigDecimal price;

    @Schema(description = "Max rental duration in days for new product", example = "14")
    private Integer expiryDate;

    @Schema(description = "Inventory number (can override InSy value)", example = "VR-001")
    private String invNumber;

    @Schema(description = "Lender ID to assign", example = "7")
    private Long lenderId;

    // ========================================
    // Fields for REJECT action
    // ========================================

    @Schema(description = "Reason for rejection (for REJECT action)",
            example = "Duplikat - Geraet existiert bereits unter anderer ID")
    private String rejectReason;

    /**
     * Action Enum
     */
    public enum Action {
        /**
         * Import the item (as new product or to existing product)
         */
        IMPORT,

        /**
         * Reject the import item
         */
        REJECT
    }
}