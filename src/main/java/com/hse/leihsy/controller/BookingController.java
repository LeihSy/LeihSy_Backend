package com.hse.leihsy.controller;

import com.hse.leihsy.model.dto.BookingDTO;
import com.hse.leihsy.model.dto.BookingStatusUpdateDTO;
import com.hse.leihsy.model.entity.User;
import com.hse.leihsy.service.BookingService;
import com.hse.leihsy.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Verwaltung von Ausleih-Buchungen")
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;

    // ========================================
    // GET ENDPOINTS
    // ========================================

    @Operation(
            summary = "Alle Buchungen abrufen mit optionalen Filtern",
            description = "Holt alle Buchungen. Optionaler Status-Filter: overdue, pending, confirmed, picked_up, returned, cancelled, expired, rejected"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Erfolgreich abgerufen"),
            @ApiResponse(responseCode = "400", description = "Ungültiger Status-Parameter"),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert")
    })
    @GetMapping
    public ResponseEntity<List<BookingDTO>> getAllBookings(
            @Parameter(
                    description = "Optional: Filter by status (overdue, pending, confirmed, picked_up, returned, cancelled, expired, rejected)",
                    example = "overdue"
            )
            @RequestParam(required = false) String status
    ) {
        List<BookingDTO> bookings = bookingService.getAllBookings(status);
        return ResponseEntity.ok(bookings);
    }

    @Operation(
            summary = "Einzelne Buchung abrufen",
            description = "Holt eine spezifische Buchung anhand ihrer ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Buchung gefunden"),
            @ApiResponse(responseCode = "404", description = "Buchung nicht gefunden"),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BookingDTO> getBookingById(
            @Parameter(description = "ID der Buchung") @PathVariable Long id) {
        BookingDTO booking = bookingService.getBookingDTOById(id);
        return ResponseEntity.ok(booking);
    }

    // ========================================
    // POST ENDPOINT - Buchung erstellen
    // ========================================

    @Operation(
            summary = "Neue Buchung erstellen",
            description = "Erstellt eine neue Buchungsanfrage für ein Item"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Buchung erfolgreich erstellt",
                    content = @Content(schema = @Schema(implementation = BookingDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Ungültige Anfrage"),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert"),
            @ApiResponse(responseCode = "404", description = "Item nicht gefunden")
    })
    @PostMapping
    public ResponseEntity<BookingDTO> createBooking(@RequestBody CreateBookingRequest request) {
        User currentUser = userService.getCurrentUser();

        BookingDTO booking = bookingService.createBooking(
                currentUser.getId(),
                request.getItemId(),
                request.getStartDate(),
                request.getEndDate(),
                request.getMessage()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    // ========================================
    // PATCH ENDPOINT - Status-Updates
    // ========================================

    @Operation(
            summary = "Booking-Status aktualisieren",
            description = "Generischer Endpoint für alle Booking-Status-Updates. Unterstützt Actions: confirm, select_pickup, propose, pickup, return"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status erfolgreich aktualisiert"),
            @ApiResponse(responseCode = "400", description = "Ungültige Anfrage oder Action"),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert"),
            @ApiResponse(responseCode = "404", description = "Buchung nicht gefunden")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<BookingDTO> updateBookingStatus(
            @Parameter(description = "ID der Buchung") @PathVariable Long id,
            @RequestBody BookingStatusUpdateDTO updateDTO
    ) {
        BookingDTO booking = bookingService.updateStatus(id, updateDTO);
        return ResponseEntity.ok(booking);
    }

    // ========================================
    // DELETE ENDPOINT - Ablehnen/Stornieren
    // ========================================

    @Operation(
            summary = "Buchung ablehnen oder stornieren",
            description = "Verleiher lehnt Buchung ab (reject) oder Student/Admin storniert (cancel). Beide Aktionen setzen deletedAt (Soft-Delete)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Buchung erfolgreich gelöscht"),
            @ApiResponse(responseCode = "400", description = "Löschen nicht möglich"),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert"),
            @ApiResponse(responseCode = "404", description = "Buchung nicht gefunden")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(
            @Parameter(description = "ID der Buchung") @PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }

    // ========================================
    // REQUEST DTOs (Inner Classes)
    // ========================================

    @Schema(description = "Anfrage zum Erstellen einer neuen Buchung")
    public static class CreateBookingRequest {
        @Schema(description = "ID des Items", example = "1")
        private Long itemId;

        @Schema(description = "Gewünschter Ausleihbeginn", example = "2025-12-10T09:00:00")
        private LocalDateTime startDate;

        @Schema(description = "Gewünschtes Ausleihende", example = "2025-12-15T17:00:00")
        private LocalDateTime endDate;

        @Schema(description = "Optionale Nachricht an den Verleiher", example = "Brauche es für Projekt")
        private String message;

        public Long getItemId() { return itemId; }
        public void setItemId(Long itemId) { this.itemId = itemId; }
        public LocalDateTime getStartDate() { return startDate; }
        public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
        public LocalDateTime getEndDate() { return endDate; }
        public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}