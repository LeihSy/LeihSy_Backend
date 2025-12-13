package com.hse.leihsy.controller;

import com.hse.leihsy.model.dto.BookingDTO;
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

    @Operation(summary = "Alle Buchungen abrufen (Admin)",
            description = "Holt alle Buchungen im System. Nur für Administratoren zugänglich.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Erfolgreich abgerufen",
                    content = @Content(schema = @Schema(implementation = BookingDTO.class))),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert"),
            @ApiResponse(responseCode = "403", description = "Keine Berechtigung - Admin-Rolle erforderlich")
    })
    @GetMapping
    public ResponseEntity<List<BookingDTO>> getAllBookings() {
        List<BookingDTO> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    @Operation(summary = "Eigene PENDING Anfragen als Verleiher abrufen",
            description = "Holt alle offenen Buchungsanfragen für den eingeloggten Verleiher")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Erfolgreich abgerufen",
                    content = @Content(schema = @Schema(implementation = BookingDTO.class))),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert")
    })
    @GetMapping("/lenders/me/pending")
    public ResponseEntity<List<BookingDTO>> getPendingBookingsForCurrentLender() {
        User currentUser = userService.getCurrentUser();
        List<BookingDTO> bookings = bookingService.getPendingBookingsByLenderId(currentUser.getId());
        return ResponseEntity.ok(bookings);
    }

    @Operation(summary = "Alle eigenen Buchungen als Verleiher abrufen",
            description = "Holt alle Buchungen für die der eingeloggte User Verleiher ist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Erfolgreich abgerufen"),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert")
    })
    @GetMapping("/lenders/me")
    public ResponseEntity<List<BookingDTO>> getMyLenderBookings() {
        User currentUser = userService.getCurrentUser();
        List<BookingDTO> bookings = bookingService.getBookingsByLenderId(currentUser.getId());
        return ResponseEntity.ok(bookings);
    }

    @Operation(summary = "Eigene Buchungen als Student abrufen",
            description = "Holt alle Buchungen die der eingeloggte User als Student/Entleiher erstellt hat")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Erfolgreich abgerufen"),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert")
    })
    @GetMapping("/users/me")
    public ResponseEntity<List<BookingDTO>> getMyBookings() {
        User currentUser = userService.getCurrentUser();
        List<BookingDTO> bookings = bookingService.getBookingsByUserId(currentUser.getId());
        return ResponseEntity.ok(bookings);
    }

    @Operation(summary = "Eigene gelöschte/stornierte Buchungen als Student abrufen",
            description = "Holt alle gelöschten/stornierten Buchungen die der eingeloggte User als Student/Entleiher erstellt hat")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Erfolgreich abgerufen",
                    content = @Content(schema = @Schema(implementation = BookingDTO.class))),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert")
    })
    @GetMapping("/users/me/deleted")
    public ResponseEntity<List<BookingDTO>> getMyDeletedBookings() {
        User currentUser = userService.getCurrentUser();
        List<BookingDTO> bookings = bookingService.getDeletedBookingsByUserId(currentUser.getId());
        return ResponseEntity.ok(bookings);
    }

    @Operation(summary = "Einzelne Buchung abrufen",
            description = "Holt eine spezifische Buchung anhand ihrer ID")
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

    @Operation(summary = "Überfällige Buchungen abrufen",
            description = "Holt alle Buchungen bei denen die Rückgabe überfällig ist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Erfolgreich abgerufen"),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert")
    })
    @GetMapping("/overdue")
    public ResponseEntity<List<BookingDTO>> getOverdueBookings() {
        List<BookingDTO> bookings = bookingService.getOverdueBookings();
        return ResponseEntity.ok(bookings);
    }

    @Operation(summary = "Buchungen eines bestimmten Users abrufen",
            description = "Holt alle Buchungen eines spezifischen Users (Admin-Funktion)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Erfolgreich abgerufen"),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert"),
            @ApiResponse(responseCode = "403", description = "Keine Berechtigung - Admin-Rolle erforderlich")
    })
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<BookingDTO>> getBookingsByUserId(
            @Parameter(description = "ID des Users") @PathVariable Long userId) {
        List<BookingDTO> bookings = bookingService.getBookingsByUserId(userId);
        return ResponseEntity.ok(bookings);
    }

    @Operation(summary = "Buchungen eines bestimmten Verleihers abrufen",
            description = "Holt alle Buchungen eines spezifischen Verleihers (Admin-Funktion)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Erfolgreich abgerufen"),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert"),
            @ApiResponse(responseCode = "403", description = "Keine Berechtigung - Admin-Rolle erforderlich")
    })
    @GetMapping("/lenders/{lenderId}")
    public ResponseEntity<List<BookingDTO>> getBookingsByLenderId(
            @Parameter(description = "ID des Verleihers") @PathVariable Long lenderId) {
        List<BookingDTO> bookings = bookingService.getBookingsByLenderId(lenderId);
        return ResponseEntity.ok(bookings);
    }

    @Operation(summary = "PENDING Buchungen eines bestimmten Verleihers abrufen",
            description = "Holt alle offenen Anfragen eines spezifischen Verleihers (Admin-Funktion)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Erfolgreich abgerufen"),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert"),
            @ApiResponse(responseCode = "403", description = "Keine Berechtigung - Admin-Rolle erforderlich")
    })
    @GetMapping("/lenders/{lenderId}/pending")
    public ResponseEntity<List<BookingDTO>> getPendingBookingsByLenderId(
            @Parameter(description = "ID des Verleihers") @PathVariable Long lenderId) {
        List<BookingDTO> bookings = bookingService.getPendingBookingsByLenderId(lenderId);
        return ResponseEntity.ok(bookings);
    }

    // ========================================
    // POST ENDPOINT - Buchung erstellen
    // ========================================

    @Operation(summary = "Neue Buchung erstellen",
            description = "Erstellt eine neue Buchungsanfrage für ein Item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Buchung erfolgreich erstellt",
                    content = @Content(schema = @Schema(implementation = BookingDTO.class))),
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
    // PUT ENDPOINTS - Buchungen aktualisieren
    // ========================================

    @Operation(summary = "Buchung bestätigen und Termine vorschlagen",
            description = "Verleiher bestätigt eine Buchungsanfrage und schlägt Abholtermine vor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Buchung erfolgreich bestätigt"),
            @ApiResponse(responseCode = "400", description = "Ungültige Anfrage"),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert"),
            @ApiResponse(responseCode = "404", description = "Buchung nicht gefunden")
    })
    @PutMapping("/{id}/confirm")
    public ResponseEntity<BookingDTO> confirmBooking(
            @Parameter(description = "ID der Buchung") @PathVariable Long id,
            @RequestBody ConfirmBookingRequest request) {

        BookingDTO booking = bookingService.confirmBooking(id, request.getProposedPickups());
        return ResponseEntity.ok(booking);
    }

    @Operation(summary = "Abholtermin auswählen",
            description = "Student wählt einen der vorgeschlagenen Abholtermine aus")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Termin erfolgreich ausgewählt"),
            @ApiResponse(responseCode = "400", description = "Ungültiger Termin"),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert"),
            @ApiResponse(responseCode = "404", description = "Buchung nicht gefunden")
    })
    @PutMapping("/{id}/select-pickup")
    public ResponseEntity<BookingDTO> selectPickupTime(
            @Parameter(description = "ID der Buchung") @PathVariable Long id,
            @RequestBody SelectPickupRequest request) {

        BookingDTO booking = bookingService.selectPickupTime(id, request.getSelectedPickup());
        return ResponseEntity.ok(booking);
    }

    @Operation(summary = "Gegenvorschlag machen (Ping-Pong)",
            description = "Student oder Verleiher macht einen neuen Terminvorschlag")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vorschlag erfolgreich gemacht"),
            @ApiResponse(responseCode = "400", description = "Ungültige Anfrage"),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert"),
            @ApiResponse(responseCode = "404", description = "Buchung nicht gefunden")
    })
    @PutMapping("/{id}/propose")
    public ResponseEntity<BookingDTO> proposeNewPickups(
            @Parameter(description = "ID der Buchung") @PathVariable Long id,
            @RequestBody ProposePickupsRequest request) {

        User currentUser = userService.getCurrentUser();

        BookingDTO booking = bookingService.proposeNewPickups(
                id,
                currentUser.getId(),
                request.getProposedPickups()
        );

        return ResponseEntity.ok(booking);
    }

    @Operation(summary = "Ausgabe dokumentieren",
            description = "Verleiher dokumentiert die Ausgabe des Items an den Student")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ausgabe erfolgreich dokumentiert"),
            @ApiResponse(responseCode = "400", description = "Ausgabe nicht möglich"),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert"),
            @ApiResponse(responseCode = "404", description = "Buchung nicht gefunden")
    })
    @PutMapping("/{id}/pickup")
    public ResponseEntity<BookingDTO> recordPickup(
            @Parameter(description = "ID der Buchung") @PathVariable Long id) {

        BookingDTO booking = bookingService.recordPickup(id);
        return ResponseEntity.ok(booking);
    }

    @Operation(summary = "Rückgabe dokumentieren",
            description = "Verleiher dokumentiert die Rückgabe des Items vom Student")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rückgabe erfolgreich dokumentiert"),
            @ApiResponse(responseCode = "400", description = "Rückgabe nicht möglich"),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert"),
            @ApiResponse(responseCode = "404", description = "Buchung nicht gefunden")
    })
    @PutMapping("/{id}/return")
    public ResponseEntity<BookingDTO> recordReturn(
            @Parameter(description = "ID der Buchung") @PathVariable Long id) {

        BookingDTO booking = bookingService.recordReturn(id);
        return ResponseEntity.ok(booking);
    }

    @Operation(summary = "Buchung ablehnen",
            description = "Verleiher lehnt eine Buchungsanfrage ab")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Buchung erfolgreich abgelehnt"),
            @ApiResponse(responseCode = "400", description = "Ablehnung nicht möglich"),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert"),
            @ApiResponse(responseCode = "404", description = "Buchung nicht gefunden")
    })
    @PutMapping("/{id}/reject")
    public ResponseEntity<BookingDTO> rejectBooking(
            @Parameter(description = "ID der Buchung") @PathVariable Long id) {

        BookingDTO booking = bookingService.rejectBooking(id);
        return ResponseEntity.ok(booking);
    }

    // ========================================
    // DELETE ENDPOINT - Buchung stornieren
    // ========================================

    @Operation(summary = "Buchung stornieren",
            description = "Student oder Admin storniert eine Buchung")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Buchung erfolgreich storniert"),
            @ApiResponse(responseCode = "400", description = "Stornierung nicht möglich"),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert"),
            @ApiResponse(responseCode = "404", description = "Buchung nicht gefunden")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(
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

        // Getters & Setters
        public Long getItemId() { return itemId; }
        public void setItemId(Long itemId) { this.itemId = itemId; }
        public LocalDateTime getStartDate() { return startDate; }
        public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
        public LocalDateTime getEndDate() { return endDate; }
        public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    @Schema(description = "Anfrage zum Bestätigen einer Buchung mit Terminvorschlägen")
    public static class ConfirmBookingRequest {
        @Schema(description = "Liste mit vorgeschlagenen Abholterminen")
        private List<LocalDateTime> proposedPickups;

        public List<LocalDateTime> getProposedPickups() { return proposedPickups; }
        public void setProposedPickups(List<LocalDateTime> proposedPickups) { this.proposedPickups = proposedPickups; }
    }

    @Schema(description = "Anfrage zum Auswählen eines Abholtermins")
    public static class SelectPickupRequest {
        @Schema(description = "Ausgewählter Abholtermin", example = "2025-12-10T09:00:00")
        private LocalDateTime selectedPickup;

        public LocalDateTime getSelectedPickup() { return selectedPickup; }
        public void setSelectedPickup(LocalDateTime selectedPickup) { this.selectedPickup = selectedPickup; }
    }

    @Schema(description = "Anfrage zum Machen eines Gegenvorschlags")
    public static class ProposePickupsRequest {
        @Schema(description = "Liste mit neuen Terminvorschlägen")
        private List<LocalDateTime> proposedPickups;

        public List<LocalDateTime> getProposedPickups() { return proposedPickups; }
        public void setProposedPickups(List<LocalDateTime> proposedPickups) { this.proposedPickups = proposedPickups; }
    }
}