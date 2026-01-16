package com.hse.leihsy.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import lombok.*;

/**
 * DTO für Item-Responses in der API.
 * Enthält alle relevanten Informationen eines physischen Exemplars.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Response DTO for a physical item (exemplar)")
public class ItemDTO {
    @Schema(description = "Location ID", example = "1")
    private Long locationId;
    
    @Schema(description = "Room number / Campus", example = "F01.402")
    private String roomNr;

    @Schema(description = "Unique identifier", example = "1")
    private Long id;

    @Schema(description = "Inventory number", example = "VR-001")
    private String invNumber;

    @Schema(description = "Owner of the item", example = "Christian Haas")
    private String owner;

    @Schema(description = "ID of the assigned lender", example = "7")
    private Long lenderId;

    @Schema(description = "Name of the assigned lender", example = "Max Mustermann")
    private String lenderName;

    @Schema(description = "ID of the product this item belongs to", example = "5")
    private Long productId;

    @Schema(description = "Name of the product", example = "Meta Quest 3")
    private String productName;

    @Schema(description = "Indicates if the item is currently available for booking")
    private Boolean isAvailable;

    @Schema(description = "Timestamp when the item was created")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the item was last updated")
    private LocalDateTime updatedAt;
}