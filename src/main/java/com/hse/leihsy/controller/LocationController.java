package com.hse.leihsy.controller;

import com.hse.leihsy.mapper.LocationMapper;
import com.hse.leihsy.model.dto.LocationDTO;
import com.hse.leihsy.model.entity.Location;
import com.hse.leihsy.repository.LocationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/locations", produces = "application/json")
@CrossOrigin(origins = "http://localhost:4200")
public class LocationController {

    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    public LocationController(LocationRepository locationRepository, LocationMapper locationMapper) {
        this.locationRepository = locationRepository;
        this.locationMapper = locationMapper;
    }


    @Operation(summary = "Get all locations", description = "Returns a list of all active locations")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Locations retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<List<LocationDTO>> getAllLocations() {
        List<Location> locations = locationRepository.findAllActive();
        List<LocationDTO> locationDTOs = locationMapper.toDTOList(locations);
        return ResponseEntity.ok(locationDTOs);
    }


    @Operation(summary = "Get location by ID", description = "Returns a location with the specified ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Location found"),
            @ApiResponse(responseCode = "404", description = "Location not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<LocationDTO> getLocationById(
            @Parameter(description = "ID of the location to retrieve") @PathVariable Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location nicht gefunden: " + id));
        LocationDTO locationDTO = locationMapper.toDTO(location);
        return ResponseEntity.ok(locationDTO);
    }
}