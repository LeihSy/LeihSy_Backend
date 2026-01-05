package com.hse.leihsy.model.dto;

import com.hse.leihsy.model.entity.InsyImportStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO fuer InsyImportItem-Responses in der API.
 * Zeigt alle relevanten Informationen fuer das Admin-Dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for an InSy import staging item")
public class InsyImportItemDTO {

    @Schema(description = "Internal ID", example = "1")
    private Long id;

    @Schema(description = "ID from InSy system", example = "12345")
    private Long insyId;

    @Schema(description = "Name of the item from InSy", example = "Meta Quest 3")
    private String name;

    @Schema(description = "Description from InSy", example = "VR-Brille mit Touch-Controllern")
    private String description;

    @Schema(description = "Location/room from InSy", example = "F01.402")
    private String location;

    @Schema(description = "Owner from InSy", example = "Christian Haas")
    private String owner;

    @Schema(description = "Inventory number from InSy", example = "VR-001")
    private String invNumber;

    @Schema(description = "Current import status", example = "PENDING")
    private InsyImportStatus status;

    @Schema(description = "ID of the product this was imported to (if imported as new product)")
    private Long importedProductId;

    @Schema(description = "Name of the product this was imported to")
    private String importedProductName;

    @Schema(description = "ID of the item this was imported/updated as")
    private Long importedItemId;

    @Schema(description = "Inventory number of the imported item")
    private String importedItemInvNumber;

    @Schema(description = "Admin note (e.g., rejection reason)")
    private String adminNote;

    @Schema(description = "Indicates if a matching product already exists in the system")
    private Boolean hasMatchingProduct;

    @Schema(description = "ID of the matching product (if exists)")
    private Long matchingProductId;

    @Schema(description = "Name of the matching product (if exists)")
    private String matchingProductName;

    @Schema(description = "Timestamp when the import item was received")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the import item was last updated")
    private LocalDateTime updatedAt;
}