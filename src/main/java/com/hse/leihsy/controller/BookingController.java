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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Verwaltung von Ausleih-Buchungen")
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*")

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
            @ApiResponse(responseCode = "400", description = "Ungueltiger Status-Parameter"),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert"),
            @ApiResponse(responseCode = "403", description = "Keine Berechtigung - nur Admins")
    })
    @PreAuthorize("hasRole('ADMIN')")
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
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert"),
            @ApiResponse(responseCode = "403", description = "Keine Berechtigung - nur Entleiher, Verleiher oder Admin")
    })
    @PreAuthorize("hasRole('ADMIN') or @bookingSecurityService.canView(#id, authentication)")
    @GetMapping("/{id}")
    public ResponseEntity<BookingDTO> getBookingById(
            @Parameter(description = "ID der Buchung") @PathVariable Long id) {
        BookingDTO booking = bookingService.getBookingDTOById(id);
        return ResponseEntity.ok(booking);
    }

    @Operation(
            summary = "Buchungen einer Gruppe abrufen",
            description = "Holt alle Buchungen einer Studentengruppe"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Buchungen gefunden"),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert"),
            @ApiResponse(responseCode = "403", description = "Keine Berechtigung - nur Gruppenmitglieder oder Admin")
    })
    @PreAuthorize("hasRole('ADMIN') or @studentGroupSecurityService.canView(#groupId, authentication)")
    @GetMapping("/groups/{groupId}")
    public ResponseEntity<List<BookingDTO>> getBookingsByGroupId(
            @Parameter(description = "ID der Studentengruppe") @PathVariable Long groupId) {
        List<BookingDTO> bookings = bookingService.getBookingsByGroupId(groupId);
        return ResponseEntity.ok(bookings);
    }

    // ========================================
    // POST ENDPOINT - Buchung erstellen
    // ========================================

    @Operation(
            summary = "Neue Buchung erstellen",
            description = "Erstellt eine neue Buchungsanfrage fuer ein Produkt. Optional kann eine Gruppen-ID angegeben werden fuer Gruppenbuchungen."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Buchung erfolgreich erstellt",
                    content = @Content(schema = @Schema(implementation = BookingDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Ungueltige Anfrage oder User nicht Mitglied der Gruppe"),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert"),
            @ApiResponse(responseCode = "404", description = "Produkt oder Gruppe nicht gefunden")
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<List<BookingDTO>> createBooking(@RequestBody CreateBookingRequest request) {
        User currentUser = userService.getCurrentUser();

        List<BookingDTO> bookings = bookingService.createBooking(
                currentUser.getId(),
                request.getProductId(),
                request.getStartDate(),
                request.getEndDate(),
                request.getMessage(),
                request.getQuantity(),
                request.getGroupId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(bookings);
    }

    // ========================================
    // PATCH ENDPOINT - Status-Updates
    // ========================================

    @Operation(
            summary = "Booking-Status aktualisieren",
            description = "Generischer Endpoint fuer alle Booking-Status-Updates. Unterstuetzt Actions: confirm, select_pickup, propose, pickup, return. Optional kann auch die Nachricht (message) aktualisiert werden."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status erfolgreich aktualisiert"),
            @ApiResponse(responseCode = "400", description = "Ungueltige Anfrage oder Action"),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert"),
            @ApiResponse(responseCode = "403", description = "Keine Berechtigung - nur Entleiher, Verleiher oder Admin"),
            @ApiResponse(responseCode = "404", description = "Buchung nicht gefunden")
    })
    @PreAuthorize("hasRole('ADMIN') or @bookingSecurityService.canUpdate(#id, authentication)")
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
            @ApiResponse(responseCode = "204", description = "Buchung erfolgreich geloescht"),
            @ApiResponse(responseCode = "400", description = "Loeschen nicht moeglich"),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert"),
            @ApiResponse(responseCode = "403", description = "Keine Berechtigung - nur Entleiher, Verleiher oder Admin"),
            @ApiResponse(responseCode = "404", description = "Buchung nicht gefunden")
    })
    @PreAuthorize("hasRole('ADMIN') or @bookingSecurityService.canDelete(#id, authentication)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(
            @Parameter(description = "ID der Buchung") @PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }

    // ========================================
    // REQUEST DTO (Inner Class)
    // ========================================

    @Schema(description = "Anfrage zum Erstellen einer neuen Buchung")
    @Getter
    @Setter
    public static class CreateBookingRequest {
        @Schema(description = "ID des Produkts", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long productId;

        @Schema(description = "Gewuenschter Ausleihbeginn", example = "2025-12-10T09:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
        private LocalDateTime startDate;

        @Schema(description = "Gewuenschtes Ausleihende", example = "2025-12-15T17:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
        private LocalDateTime endDate;

        @Schema(description = "Optionale Nachricht an den Verleiher", example = "Brauche es fuer Projekt")
        private String message;

        @Schema(description = "Gewünschte Anzahl", example = "4")
        private int quantity;

        @Schema(description = "Optionale Gruppen-ID fuer Gruppenbuchungen. NULL = Einzelbuchung", example = "5")
        private Long groupId;
    }
}