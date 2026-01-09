package com.hse.leihsy.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO für generische Booking-Status-Updates via PATCH /api/bookings/{id}
 * Unterstützt alle Status-Übergänge mit einem einzigen Endpoint
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO für Booking-Status-Updates")
public class BookingStatusUpdateDTO {

    @Schema(
            description = "Action to perform: confirm, select_pickup, propose, pickup, return",
            example = "pickup",
            required = true
    )
    private String action;

    @Schema(
            description = "List of proposed pickup times (required for 'confirm' and 'propose' actions)",
            example = "[\"2025-12-20T10:00:00\", \"2025-12-20T14:00:00\"]"
    )
    private List<LocalDateTime> proposedPickups;

    @Schema(
            description = "Selected pickup time (required for 'select_pickup' action)",
            example = "2025-12-20T10:00:00"
    )
    private LocalDateTime selectedPickup;

    @Schema(
            description = "Optional: Update the booking message",
            example = "Aktualisierte Nachricht"
    )
    private String message;
}
