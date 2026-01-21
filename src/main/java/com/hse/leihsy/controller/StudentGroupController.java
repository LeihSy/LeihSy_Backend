package com.hse.leihsy.controller;

import com.hse.leihsy.model.dto.CreateStudentGroupDTO;
import com.hse.leihsy.model.dto.StudentGroupDTO;
import com.hse.leihsy.model.dto.UpdateStudentGroupDTO;
import com.hse.leihsy.service.StudentGroupService;
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

/**
 * REST Controller für StudentGroup Verwaltung
 */
@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Tag(name = "Student Groups", description = "Verwaltung von Studentengruppen für gemeinsame Ausleihen")
public class StudentGroupController {

    private final StudentGroupService groupService;

    // ========================================
    // GET ENDPOINTS
    // ========================================

    @Operation(
            summary = "Gruppe per ID abrufen",
            description = "Holt eine spezifische Gruppe anhand ihrer ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gruppe gefunden"),
            @ApiResponse(responseCode = "404", description = "Gruppe nicht gefunden"),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert"),
            @ApiResponse(responseCode = "403", description = "Keine Berechtigung - nur Mitglieder oder Admin")
    })
    @PreAuthorize("hasRole('ADMIN') or @studentGroupSecurityService.canView(#id, authentication)")
    @GetMapping("/{id}")
    public ResponseEntity<StudentGroupDTO> getGroupById(
            @Parameter(description = "ID der Gruppe") @PathVariable Long id) {
        StudentGroupDTO group = groupService.getGroupById(id);
        return ResponseEntity.ok(group);
    }

    @Operation(
            summary = "Alle Gruppen abrufen oder suchen",
            description = "Gibt alle aktiven Gruppen zurück. Mit Query-Parameter 'q' kann nach Namen gesucht werden."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Erfolgreiche Abfrage"),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert")
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<StudentGroupDTO>> getAllGroups(
            @Parameter(description = "Suchbegriff für Gruppenname (optional)")
            @RequestParam(required = false) String q) {

        List<StudentGroupDTO> groups;
        if (q != null && !q.isBlank()) {
            groups = groupService.searchGroups(q);
        } else {
            groups = groupService.getAllGroups();
        }
        return ResponseEntity.ok(groups);
    }

    // ========================================
    // POST ENDPOINT - Gruppe erstellen
    // ========================================

    @Operation(
            summary = "Neue Gruppe erstellen",
            description = "Erstellt eine neue Studentengruppe. Der aktuelle User wird automatisch als Ersteller und erstes Mitglied gesetzt."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Gruppe erfolgreich erstellt",
                    content = @Content(schema = @Schema(implementation = StudentGroupDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Ungültige Anfrage (z.B. Name bereits vergeben)"),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert")
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<StudentGroupDTO> createGroup(
            @Valid @RequestBody CreateStudentGroupDTO createDTO) {
        StudentGroupDTO group = groupService.createGroup(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(group);
    }

    // ========================================
    // PUT/PATCH ENDPOINTS - Gruppe bearbeiten
    // ========================================

    @Operation(
            summary = "Gruppe aktualisieren",
            description = "Aktualisiert Name und/oder Beschreibung einer Gruppe (nur Owner)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gruppe erfolgreich aktualisiert"),
            @ApiResponse(responseCode = "400", description = "Ungültige Anfrage"),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert"),
            @ApiResponse(responseCode = "403", description = "Keine Berechtigung (nicht Owner)"),
            @ApiResponse(responseCode = "404", description = "Gruppe nicht gefunden")
    })
    @PreAuthorize("hasRole('ADMIN') or @studentGroupSecurityService.canUpdate(#id, authentication)")
    @PatchMapping("/{id}")
    public ResponseEntity<StudentGroupDTO> updateGroup(
            @Parameter(description = "ID der Gruppe") @PathVariable Long id,
            @Valid @RequestBody UpdateStudentGroupDTO updateDTO) {
        StudentGroupDTO group = groupService.updateGroup(id, updateDTO);
        return ResponseEntity.ok(group);
    }

    // ========================================
    // MEMBER MANAGEMENT ENDPOINTS
    // ========================================

    @Operation(
            summary = "Mitglied hinzufügen",
            description = "Fügt einen User als Mitglied zur Gruppe hinzu. Jeder User kann sich selbst hinzufügen, nur der Owner kann andere User hinzufügen."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mitglied erfolgreich hinzugefügt"),
            @ApiResponse(responseCode = "400", description = "User ist bereits Mitglied"),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert"),
            @ApiResponse(responseCode = "403", description = "Keine Berechtigung (kann nur sich selbst oder als Owner andere hinzufügen)"),
            @ApiResponse(responseCode = "404", description = "Gruppe oder User nicht gefunden")
    })
    @PreAuthorize("hasRole('ADMIN') or @studentGroupSecurityService.canManageMembers(#groupId, #userId, authentication)")
    @PostMapping("/{groupId}/members/{userId}")
    public ResponseEntity<StudentGroupDTO> addMember(
            @Parameter(description = "ID der Gruppe") @PathVariable Long groupId,
            @Parameter(description = "ID des Users") @PathVariable Long userId) {
        StudentGroupDTO group = groupService.addMember(groupId, userId);
        return ResponseEntity.ok(group);
    }

    @Operation(
            summary = "Mitglied entfernen",
            description = "Entfernt einen User aus der Gruppe (Owner oder das Mitglied selbst). Der Ersteller kann nicht entfernt werden."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mitglied erfolgreich entfernt"),
            @ApiResponse(responseCode = "400", description = "Ersteller kann nicht entfernt werden"),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert"),
            @ApiResponse(responseCode = "403", description = "Keine Berechtigung"),
            @ApiResponse(responseCode = "404", description = "Gruppe oder User nicht gefunden")
    })
    @PreAuthorize("hasRole('ADMIN') or @studentGroupSecurityService.canManageMembers(#groupId, #userId, authentication)")
    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<StudentGroupDTO> removeMember(
            @Parameter(description = "ID der Gruppe") @PathVariable Long groupId,
            @Parameter(description = "ID des Users") @PathVariable Long userId) {
        StudentGroupDTO group = groupService.removeMember(groupId, userId);
        return ResponseEntity.ok(group);
    }

    // ========================================
    // DELETE ENDPOINT - Gruppe löschen
    // ========================================

    @Operation(
            summary = "Gruppe löschen",
            description = "Löscht eine Gruppe (Soft-Delete, nur Owner). Nicht möglich wenn noch aktive Buchungen existieren."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Gruppe erfolgreich gelöscht"),
            @ApiResponse(responseCode = "400", description = "Löschen nicht möglich (aktive Buchungen)"),
            @ApiResponse(responseCode = "401", description = "Nicht authentifiziert"),
            @ApiResponse(responseCode = "403", description = "Keine Berechtigung (nicht Owner)"),
            @ApiResponse(responseCode = "404", description = "Gruppe nicht gefunden")
    })
    @PreAuthorize("hasRole('ADMIN') or @studentGroupSecurityService.canDelete(#id, authentication)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(
            @Parameter(description = "ID der Gruppe") @PathVariable Long id) {
        groupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }
}