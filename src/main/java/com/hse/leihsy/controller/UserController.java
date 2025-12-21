package com.hse.leihsy.controller;

import com.hse.leihsy.model.dto.UserDTO;
import com.hse.leihsy.model.entity.User;
import com.hse.leihsy.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.hse.leihsy.model.dto.BookingDTO;
import com.hse.leihsy.service.BookingService;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Benutzerverwaltung")
public class UserController {

    private final UserService userService;
    private final BookingService bookingService;

    public UserController(UserService userService, BookingService bookingService) {
        this.userService = userService;
        this.bookingService = bookingService;
    }

    /**
     * Gibt den aktuell eingeloggten User zurueck inkl. Rollen aus Keycloak
     */
    @Operation(summary = "Aktuellen User abrufen",
            description = "Gibt den eingeloggten User mit seinen Rollen zurueck")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User gefunden",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert")
    })
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        User user = userService.getCurrentUser();

        // Rollen aus Security Context holen
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        UserDTO dto = convertToDTO(user, roles);
        return ResponseEntity.ok(dto);
    }

    /**
     * Gibt einen User anhand seiner ID zurueck (nur fuer Admins)
     */
    @Operation(summary = "User per ID abrufen",
            description = "Gibt einen User anhand seiner Datenbank-ID zurueck")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User gefunden"),
            @ApiResponse(responseCode = "404", description = "User nicht gefunden")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        UserDTO dto = convertToDTO(user, List.of());
        return ResponseEntity.ok(dto);
    }

    @Operation(
            summary = "Get bookings of a user",
            description = "Returns all bookings for a specific user. Admins can view any user, users can only view their own bookings."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}/bookings")
    public ResponseEntity<List<BookingDTO>> getUserBookings(
            @Parameter(description = "ID of the user") @PathVariable Long userId,
            @Parameter(description = "Include soft-deleted bookings (admin only)")
            @RequestParam(required = false, defaultValue = "false") boolean deleted
    ) {
        List<BookingDTO> bookings = bookingService.getBookingsByUserId(userId, deleted);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Gibt einen User anhand seines Benutzernamens (name) zurueck (nur fuer Admins)
     */
    @Operation(summary = "User per Benutzername abrufen",
            description = "Gibt einen User anhand seines Benutzernamens zurueck")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User gefunden",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "User nicht gefunden")
    })
    @GetMapping("/names/{name}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String name) {
        User user = userService.getUserByName(name);
        UserDTO dto = convertToDTO(user, List.of());
        return ResponseEntity.ok(dto);
    }

    /**
     * Konvertiert User Entity zu DTO
     */
    private UserDTO convertToDTO(User user, List<String> roles) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUniqueId(user.getUniqueId());
        dto.setName(user.getName());
        dto.setBudget(user.getBudget());
        dto.setRoles(roles);
        return dto;
    }
}