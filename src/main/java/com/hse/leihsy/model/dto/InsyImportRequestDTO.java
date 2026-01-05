package com.hse.leihsy.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO fuer die Import-Aktion durch den Admin.
 * Enthaelt alle Informationen die der Admin beim Import angeben kann.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for importing an InSy item into the system")
public class InsyImportRequestDTO {

    @NotNull(message = "Import-Item-ID ist erforderlich")
    @Schema(description = "ID of the InsyImportItem to import", example = "1", required = true)
    private Long importItemId;

    @NotNull(message = "Import-Typ ist erforderlich")
    @Schema(description = "Type of import: NEW_PRODUCT (create new product + item) or EXISTING_PRODUCT (add item to existing product)",
            example = "EXISTING_PRODUCT", required = true)
    private ImportType importType;

    // Felder fuer EXISTING_PRODUCT
    @Schema(description = "ID of existing product to add item to (required for EXISTING_PRODUCT)", example = "5")
    private Long existingProductId;

    // Felder fuer NEW_PRODUCT
    @Schema(description = "Category ID for new product (required for NEW_PRODUCT)", example = "1")
    private Long categoryId;

    @Schema(description = "Location ID for new product (required for NEW_PRODUCT)", example = "2")
    private Long locationId;

    @Schema(description = "Price per day for new product", example = "5.00")
    private BigDecimal price;

    @Schema(description = "Max rental duration in days for new product", example = "14")
    private Integer expiryDate;

    // Gemeinsame Felder
    @Schema(description = "Inventory number for the new item (can override InSy value)", example = "VR-001")
    private String invNumber;

    @Schema(description = "Lender ID to assign to the new item", example = "7")
    private Long lenderId;

    /**
     * Import-Typ Enum
     */
    public enum ImportType {
        /**
         * Neues Product + Item erstellen
         */
        NEW_PRODUCT,

        /**
         * Item zu bestehendem Product hinzufuegen
         */
        EXISTING_PRODUCT
    }
}