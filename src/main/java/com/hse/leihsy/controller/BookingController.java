package com.hse.leihsy.controller;

import com.hse.leihsy.mapper.BookingMapper;
import com.hse.leihsy.model.dto.BookingDTO;
import com.hse.leihsy.model.dto.BookingCreateRequestDTO;
import com.hse.leihsy.model.dto.BookingConfirmRequestDTO;
import com.hse.leihsy.model.dto.BookingSelectPickupRequestDTO;
import com.hse.leihsy.model.dto.BookingProposeRequestDTO;
import com.hse.leihsy.model.entity.Booking;
import com.hse.leihsy.model.entity.User;
import com.hse.leihsy.service.BookingService;
import com.hse.leihsy.service.JwtService;
import com.hse.leihsy.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Verwaltung von Ausleih-Buchungen")
public class BookingController {

    private final BookingService bookingService;
    private final BookingMapper bookingMapper;
    private final JwtService jwtService;
    private final UserService userService;

    // ========================================
    // GET ENDPOINTS
    // ========================================

    @GetMapping("/{id}")
    @Operation(
            summary = "Booking nach ID abrufen",
            description = "Ruft eine einzelne Buchung anhand ihrer ID ab. Nutzer können nur ihre eigenen Bookings sehen (außer Admins/Lender)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Booking erfolgreich abgerufen",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookingDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Booking nicht gefunden"),
            @ApiResponse(responseCode = "403", description = "Zugriff verweigert")
    })
    public ResponseEntity<BookingDTO> getBookingById(
            @Parameter(description = "ID der Buchung", required = true, example = "1")
            @PathVariable Long id) {

        Booking booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(bookingMapper.toDTO(booking));
    }

    @GetMapping("/users/me")
    @Operation(
            summary = "Eigene Bookings abrufen (als Student)",
            description = "Ruft alle Buchungen des aktuell eingeloggten Nutzers ab (als Entleiher/Student)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste der eigenen Bookings",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookingDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert")
    })
    public ResponseEntity<List<BookingDTO>> getMyBookings() {
        String uniqueId = jwtService.getCurrentUserUniqueId();
        User user = userService.getOrCreateUser(
                uniqueId,
                jwtService.getCurrentUsername(),
                jwtService.getCurrentUserEmail()
        );

        List<Booking> bookings = bookingService.getBookingsByUserId(user.getId());
        return ResponseEntity.ok(bookingMapper.toDTOList(bookings));
    }

    @GetMapping("/users/{userId}")
    @Operation(
            summary = "Bookings eines bestimmten Users abrufen",
            description = "Nur für Admins: Ruft alle Buchungen eines bestimmten Nutzers ab."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste der Bookings des Users",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookingDTO.class))
            ),
            @ApiResponse(responseCode = "403", description = "Keine Admin-Berechtigung"),
            @ApiResponse(responseCode = "404", description = "User nicht gefunden")
    })
    public ResponseEntity<List<BookingDTO>> getBookingsByUserId(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long userId) {

        List<Booking> bookings = bookingService.getBookingsByUserId(userId);
        return ResponseEntity.ok(bookingMapper.toDTOList(bookings));
    }

    @GetMapping("/lenders/me")
    @Operation(
            summary = "Eigene Bookings abrufen (als Verleiher)",
            description = "Ruft alle Buchungen ab, bei denen der aktuelle Nutzer als Verleiher eingetragen ist."
    )
    @PreAuthorize("hasAnyRole('LENDER', 'ADMIN')")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste der eigenen Verleiher-Bookings",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookingDTO.class))
            ),
            @ApiResponse(responseCode = "403", description = "Keine Lender-Berechtigung")
    })
    public ResponseEntity<List<BookingDTO>> getMyLenderBookings() {
        String uniqueId = jwtService.getCurrentUserUniqueId();
        User user = userService.getOrCreateUser(
                uniqueId,
                jwtService.getCurrentUsername(),
                jwtService.getCurrentUserEmail()
        );

        List<Booking> bookings = bookingService.getBookingsByLenderId(user.getId());
        return ResponseEntity.ok(bookingMapper.toDTOList(bookings));
    }

    @GetMapping("/lenders/{lenderId}")
    @Operation(
            summary = "Bookings eines bestimmten Verleihers abrufen",
            description = "Nur für Admins: Ruft alle Buchungen eines bestimmten Verleihers ab."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste der Bookings des Verleihers",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookingDTO.class))
            ),
            @ApiResponse(responseCode = "403", description = "Keine Admin-Berechtigung"),
            @ApiResponse(responseCode = "404", description = "Lender nicht gefunden")
    })
    public ResponseEntity<List<BookingDTO>> getBookingsByLenderId(
            @Parameter(description = "Lender ID", required = true, example = "2")
            @PathVariable Long lenderId) {

        List<Booking> bookings = bookingService.getBookingsByLenderId(lenderId);
        return ResponseEntity.ok(bookingMapper.toDTOList(bookings));
    }

    @GetMapping("/lenders/me/pending")
    @Operation(
            summary = "Offene Anfragen als Verleiher abrufen",
            description = "Ruft alle Bookings im Status PENDING ab, bei denen der aktuelle Nutzer Verleiher ist."
    )
    @PreAuthorize("hasAnyRole('LENDER', 'ADMIN')")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste der offenen Anfragen",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookingDTO.class))
            ),
            @ApiResponse(responseCode = "403", description = "Keine Lender-Berechtigung")
    })
    public ResponseEntity<List<BookingDTO>> getMyPendingBookings() {
        String uniqueId = jwtService.getCurrentUserUniqueId();
        User user = userService.getOrCreateUser(
                uniqueId,
                jwtService.getCurrentUsername(),
                jwtService.getCurrentUserEmail()
        );

        List<Booking> bookings = bookingService.getPendingBookingsByLenderId(user.getId());
        return ResponseEntity.ok(bookingMapper.toDTOList(bookings));
    }

    @GetMapping("/lenders/{lenderId}/pending")
    @Operation(
            summary = "Offene Anfragen eines bestimmten Verleihers abrufen",
            description = "Nur für Admins: Ruft alle offenen Buchungsanfragen eines Verleihers ab."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste der offenen Anfragen",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookingDTO.class))
            ),
            @ApiResponse(responseCode = "403", description = "Keine Admin-Berechtigung")
    })
    public ResponseEntity<List<BookingDTO>> getPendingBookingsByLenderId(
            @Parameter(description = "Lender ID", required = true, example = "2")
            @PathVariable Long lenderId) {

        List<Booking> bookings = bookingService.getPendingBookingsByLenderId(lenderId);
        return ResponseEntity.ok(bookingMapper.toDTOList(bookings));
    }

    @GetMapping("/overdue")
    @Operation(
            summary = "Überfällige Bookings abrufen",
            description = "Ruft alle Bookings ab, bei denen die Rückgabe überfällig ist."
    )
    @PreAuthorize("hasAnyRole('LENDER', 'ADMIN')")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste der überfälligen Bookings",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookingDTO.class))
            ),
            @ApiResponse(responseCode = "403", description = "Keine Lender/Admin-Berechtigung")
    })
    public ResponseEntity<List<BookingDTO>> getOverdueBookings() {
        List<Booking> bookings = bookingService.getOverdueBookings();
        return ResponseEntity.ok(bookingMapper.toDTOList(bookings));
    }

    // ========================================
    // POST ENDPOINTS
    // ========================================

    @PostMapping
    @Operation(
            summary = "Neue Booking erstellen",
            description = "Erstellt eine neue Buchungsanfrage. Der aktuell eingeloggte User wird automatisch als Entleiher eingetragen."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Booking erfolgreich erstellt",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookingDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Ungültige Eingabedaten"),
            @ApiResponse(responseCode = "404", description = "Item nicht gefunden"),
            @ApiResponse(responseCode = "409", description = "Item nicht verfügbar im gewünschten Zeitraum")
    })
    public ResponseEntity<BookingDTO> createBooking(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Booking-Daten",
                    required = true,
                    content = @Content(schema = @Schema(implementation = BookingCreateRequestDTO.class))
            )
            @Valid @RequestBody BookingCreateRequestDTO request) {

        String uniqueId = jwtService.getCurrentUserUniqueId();
        User user = userService.getOrCreateUser(
                uniqueId,
                jwtService.getCurrentUsername(),
                jwtService.getCurrentUserEmail()
        );

        Booking booking = bookingService.createBooking(
                user.getId(),
                request.getItemId(),
                request.getStartDate(),
                request.getEndDate(),
                request.getMessage()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingMapper.toDTO(booking));
    }

    // ========================================
    // PUT ENDPOINTS
    // ========================================

    @PutMapping("/{id}/confirm")
    @Operation(
            summary = "Booking bestätigen und Termine vorschlagen",
            description = "Verleiher bestätigt die Anfrage und schlägt mögliche Abholtermine vor. Status wechselt zu CONFIRMED."
    )
    @PreAuthorize("hasAnyRole('LENDER', 'ADMIN')")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Booking bestätigt",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookingDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Booking bereits bestätigt oder ungültiger Status"),
            @ApiResponse(responseCode = "403", description = "Keine Lender-Berechtigung"),
            @ApiResponse(responseCode = "404", description = "Booking nicht gefunden")
    })
    public ResponseEntity<BookingDTO> confirmBooking(
            @Parameter(description = "Booking ID", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Liste vorgeschlagener Abholtermine",
                    required = true,
                    content = @Content(schema = @Schema(implementation = BookingConfirmRequestDTO.class))
            )
            @Valid @RequestBody BookingConfirmRequestDTO request) {

        Booking booking = bookingService.confirmBooking(id, request.getProposedPickups());
        return ResponseEntity.ok(bookingMapper.toDTO(booking));
    }

    @PutMapping("/{id}/select-pickup")
    @Operation(
            summary = "Abholtermin auswählen",
            description = "Student wählt einen der vorgeschlagenen Abholtermine aus. Dieser wird als confirmed_pickup gespeichert."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Termin ausgewählt",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookingDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Termin nicht in vorgeschlagenen Terminen enthalten"),
            @ApiResponse(responseCode = "403", description = "Nur der Entleiher darf einen Termin auswählen"),
            @ApiResponse(responseCode = "404", description = "Booking nicht gefunden")
    })
    public ResponseEntity<BookingDTO> selectPickupTime(
            @Parameter(description = "Booking ID", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Ausgewählter Abholtermin",
                    required = true,
                    content = @Content(schema = @Schema(implementation = BookingSelectPickupRequestDTO.class))
            )
            @Valid @RequestBody BookingSelectPickupRequestDTO request) {

        Booking booking = bookingService.selectPickupTime(id, request.getSelectedPickup());
        return ResponseEntity.ok(bookingMapper.toDTO(booking));
    }

    @PutMapping("/{id}/propose")
    @Operation(
            summary = "Gegenvorschlag machen (Ping-Pong)",
            description = "Student oder Verleiher macht einen Gegenvorschlag für Abholtermine. Die proposed_pickups werden überschrieben."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Gegenvorschlag gespeichert",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookingDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Booking bereits abgeschlossen oder storniert"),
            @ApiResponse(responseCode = "404", description = "Booking nicht gefunden")
    })
    public ResponseEntity<BookingDTO> proposeNewPickups(
            @Parameter(description = "Booking ID", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Neue Terminvorschläge",
                    required = true,
                    content = @Content(schema = @Schema(implementation = BookingProposeRequestDTO.class))
            )
            @Valid @RequestBody BookingProposeRequestDTO request) {

        String uniqueId = jwtService.getCurrentUserUniqueId();
        User proposer = userService.getOrCreateUser(
                uniqueId,
                jwtService.getCurrentUsername(),
                jwtService.getCurrentUserEmail()
        );

        Booking booking = bookingService.proposeNewPickups(
                id,
                proposer.getId(),
                request.getProposedPickups()
        );

        return ResponseEntity.ok(bookingMapper.toDTO(booking));
    }

    @PutMapping("/{id}/reject")
    @Operation(
            summary = "Booking ablehnen",
            description = "Verleiher lehnt die Buchungsanfrage ab. Booking wird als REJECTED markiert (Soft-Delete)."
    )
    @PreAuthorize("hasAnyRole('LENDER', 'ADMIN')")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Booking abgelehnt",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookingDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Booking bereits abgeschlossen"),
            @ApiResponse(responseCode = "403", description = "Keine Lender-Berechtigung"),
            @ApiResponse(responseCode = "404", description = "Booking nicht gefunden")
    })
    public ResponseEntity<BookingDTO> rejectBooking(
            @Parameter(description = "Booking ID", required = true, example = "1")
            @PathVariable Long id) {

        Booking booking = bookingService.rejectBooking(id);
        return ResponseEntity.ok(bookingMapper.toDTO(booking));
    }

    @PutMapping("/{id}/pickup")
    @Operation(
            summary = "Ausgabe dokumentieren",
            description = "Verleiher dokumentiert die Ausgabe des Items. Distribution_date wird gesetzt, Status wechselt zu PICKED_UP."
    )
    @PreAuthorize("hasAnyRole('LENDER', 'ADMIN')")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Ausgabe dokumentiert",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookingDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Booking nicht bestätigt oder bereits ausgegeben"),
            @ApiResponse(responseCode = "403", description = "Keine Lender-Berechtigung"),
            @ApiResponse(responseCode = "404", description = "Booking nicht gefunden")
    })
    public ResponseEntity<BookingDTO> recordPickup(
            @Parameter(description = "Booking ID", required = true, example = "1")
            @PathVariable Long id) {

        Booking booking = bookingService.recordPickup(id);
        return ResponseEntity.ok(bookingMapper.toDTO(booking));
    }

    @PutMapping("/{id}/return")
    @Operation(
            summary = "Rückgabe dokumentieren",
            description = "Verleiher dokumentiert die Rückgabe des Items. Return_date wird gesetzt, Status wechselt zu RETURNED."
    )
    @PreAuthorize("hasAnyRole('LENDER', 'ADMIN')")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Rückgabe dokumentiert",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookingDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Item nicht ausgegeben oder bereits zurückgegeben"),
            @ApiResponse(responseCode = "403", description = "Keine Lender-Berechtigung"),
            @ApiResponse(responseCode = "404", description = "Booking nicht gefunden")
    })
    public ResponseEntity<BookingDTO> recordReturn(
            @Parameter(description = "Booking ID", required = true, example = "1")
            @PathVariable Long id) {

        Booking booking = bookingService.recordReturn(id);
        return ResponseEntity.ok(bookingMapper.toDTO(booking));
    }

    // ========================================
    // DELETE ENDPOINTS
    // ========================================

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Booking stornieren",
            description = "Student oder Admin storniert eine Booking. Status wechselt zu CANCELLED (Soft-Delete)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Booking storniert"),
            @ApiResponse(responseCode = "400", description = "Booking kann nicht mehr storniert werden"),
            @ApiResponse(responseCode = "403", description = "Keine Berechtigung zum Stornieren"),
            @ApiResponse(responseCode = "404", description = "Booking nicht gefunden")
    })
    public ResponseEntity<Void> cancelBooking(
            @Parameter(description = "Booking ID", required = true, example = "1")
            @PathVariable Long id) {

        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }
}