package com.hse.leihsy.controller;

import com.hse.leihsy.mapper.LocationMapper;
import com.hse.leihsy.model.dto.LocationDTO;
import com.hse.leihsy.model.entity.Location;
import com.hse.leihsy.repository.ItemRepository;
import com.hse.leihsy.repository.LocationRepository;
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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/locations", produces = "application/json")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
@Tag(name = "Location Management", description = "APIs for managing storage locations")
public class LocationController {

    private final LocationRepository locationRepository;
    private final ItemRepository itemRepository;
    private final LocationMapper locationMapper;

    @Operation(
            summary = "Get all locations",
            description = "Returns a list of all active locations. Only locations with at least one assigned item are displayed."
    )
    @ApiResponse(responseCode = "200", description = "Locations retrieved successfully")
    @GetMapping
    public ResponseEntity<List<LocationDTO>> getAllLocations() {
        List<Location> locations = locationRepository.findAllActive();
        List<LocationDTO> locationDTOs = locationMapper.toDTOList(locations);
        return ResponseEntity.ok(locationDTOs);
    }

    @Operation(
            summary = "Get location by ID",
            description = "Returns a location with the specified ID"
    )
    @ApiResponse(responseCode = "200", description = "Location found")
    @ApiResponse(responseCode = "404", description = "Location not found")
    @GetMapping("/{id}")
    public ResponseEntity<LocationDTO> getLocationById(
            @Parameter(description = "ID of the location to retrieve") @PathVariable Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location nicht gefunden: " + id));
        LocationDTO locationDTO = locationMapper.toDTO(location);
        return ResponseEntity.ok(locationDTO);
    }

    @Operation(
            summary = "Create a new location",
            description = "Creates a new storage location with the specified room number"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Location created successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Location.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid request body",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{\"error\": \"Room number is required\"}")
            )
    )
    @PostMapping
    public ResponseEntity<?> createLocation(
            @Parameter(
                    description = "Location data with room number",
                    required = true,
                    schema = @Schema(example = "{\"roomNr\": \"F01.402\"}")
            )
            @RequestBody Map<String, String> request
    ) {
        String roomNr = request.get("roomNr");

        if (roomNr == null || roomNr.isBlank()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Room number is required"));
        }

        Location location = new Location();
        location.setRoomNr(roomNr);

        Location savedLocation = locationRepository.save(location);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedLocation);
    }

    @Operation(
            summary = "Delete a location",
            description = "Deletes a location if no items are assigned to it. Uses soft-delete (sets deletedAt timestamp)."
    )
    @ApiResponse(
            responseCode = "204",
            description = "Location deleted successfully"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Location has assigned items and cannot be deleted",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{\"error\": \"Cannot delete location: 3 items are still assigned to this location\"}")
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Location not found"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLocation(
            @Parameter(description = "ID of the location to delete") @PathVariable Long id
    ) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location nicht gefunden: " + id));

        // Prüfen ob Items existieren
        long itemCount = itemRepository.countByProductLocationId(id);

        if (itemCount > 0) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "error",
                            String.format("Cannot delete location: %d items are still assigned to this location", itemCount)
                    ));
        }

        // Soft-Delete
        location.softDelete();
        locationRepository.save(location);

        return ResponseEntity.noContent().build();
    }
}