package com.hse.leihsy.controller;

import com.hse.leihsy.model.dto.*;
import com.hse.leihsy.model.entity.InsyImportStatus;
import com.hse.leihsy.service.InsyImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/insy", produces = "application/json")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
@Tag(name = "insy-import-controller", description = "APIs for managing InSy imports")
public class InsyImportController {

    private final InsyImportService importService;

    // ========================================
    // GET ENDPOINTS
    // ========================================

    @Operation(summary = "Get all imports",
            description = "Returns all import items. Use ?status=PENDING|IMPORTED|REJECTED|UPDATED to filter.")
    @ApiResponse(responseCode = "200", description = "Imports retrieved successfully")
    @GetMapping("/imports")
    public ResponseEntity<List<InsyImportItemDTO>> getAllImports(
            @Parameter(description = "Filter by status") @RequestParam(required = false) InsyImportStatus status
    ) {
        if (status == InsyImportStatus.PENDING) {
            return ResponseEntity.ok(importService.getAllPending());
        }
        return ResponseEntity.ok(importService.getAll(status));
    }

    @Operation(summary = "Get import count",
            description = "Returns the count of imports. Use ?status=PENDING for badge display.")
    @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    @GetMapping("/imports/count")
    public ResponseEntity<Map<String, Long>> getImportCount(
            @Parameter(description = "Filter by status") @RequestParam(required = false, defaultValue = "PENDING") InsyImportStatus status
    ) {
        if (status == InsyImportStatus.PENDING) {
            return ResponseEntity.ok(Map.of("count", importService.countPending()));
        }
        return ResponseEntity.ok(Map.of("count", importService.countPending()));
    }

    @Operation(summary = "Get import by ID", description = "Returns a single import item by its ID")
    @ApiResponse(responseCode = "200", description = "Import found")
    @ApiResponse(responseCode = "404", description = "Import not found")
    @GetMapping("/imports/{id}")
    public ResponseEntity<InsyImportItemDTO> getImportById(
            @Parameter(description = "ID of the import item") @PathVariable Long id
    ) {
        return ResponseEntity.ok(importService.getById(id));
    }

    // ========================================
    // POST ENDPOINTS - Receive from InSy
    // ========================================

    @Operation(summary = "Receive data from InSy",
            description = "Endpoint for InSy to push item data. Creates new import item or updates existing PENDING one.")
    @ApiResponse(responseCode = "201", description = "Data received and stored successfully")
    @ApiResponse(responseCode = "400", description = "Invalid data or item already processed")
    @PostMapping("/imports")
    public ResponseEntity<InsyImportItemDTO> receiveFromInsy(
            @Parameter(description = "Item data from InSy") @Valid @RequestBody InsyImportPushDTO pushData
    ) {
        var importItem = importService.receiveFromInsy(pushData);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                importService.getById(importItem.getId())
        );
    }

    @Operation(summary = "Receive multiple items from InSy",
            description = "Endpoint for InSy to push multiple items at once")
    @ApiResponse(responseCode = "201", description = "Data received successfully")
    @PostMapping("/imports/bulk")
    public ResponseEntity<Map<String, Object>> receiveMultipleFromInsy(
            @Parameter(description = "List of items from InSy") @Valid @RequestBody List<InsyImportPushDTO> pushDataList
    ) {
        var results = importService.receiveMultipleFromInsy(pushDataList);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "received", results.size(),
                "total", pushDataList.size()
        ));
    }

    // ========================================
    // PATCH ENDPOINTS - Status Changes
    // ========================================

    @Operation(summary = "Update import status (import or reject)",
            description = "Changes the status of an import item. Use action=IMPORT to import, action=REJECT to reject. " +
                    "If an item with the same inventory number exists, it will be updated instead of creating a duplicate.")
    @ApiResponse(responseCode = "200", description = "Status updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request or item already processed")
    @ApiResponse(responseCode = "404", description = "Import item or referenced entities not found")
    @PatchMapping("/imports/{id}")
    public ResponseEntity<InsyImportItemDTO> updateImportStatus(
            @Parameter(description = "ID of the import item") @PathVariable Long id,
            @Parameter(description = "Status update data") @Valid @RequestBody InsyImportStatusUpdateDTO request
    ) {
        // ID aus Path in Request uebernehmen
        request.setImportItemId(id);

        if (request.getAction() == InsyImportStatusUpdateDTO.Action.REJECT) {
            InsyRejectRequestDTO rejectRequest = InsyRejectRequestDTO.builder()
                    .importItemId(id)
                    .reason(request.getRejectReason())
                    .build();
            return ResponseEntity.ok(importService.rejectItem(rejectRequest));
        } else {
            // IMPORT action
            InsyImportRequestDTO importRequest = InsyImportRequestDTO.builder()
                    .importItemId(id)
                    .importType(request.getImportType())
                    .existingProductId(request.getExistingProductId())
                    .categoryId(request.getCategoryId())
                    .locationId(request.getLocationId())
                    .price(request.getPrice())
                    .expiryDate(request.getExpiryDate())
                    .invNumber(request.getInvNumber())
                    .lenderId(request.getLenderId())
                    .build();
            return ResponseEntity.ok(importService.importItem(importRequest));
        }
    }

    @Operation(summary = "Batch update import status",
            description = "Imports multiple InSy items to the same Product. " +
                    "All items must be in PENDING status. Existing items will be updated.")
    @ApiResponse(responseCode = "200", description = "Items imported successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request or items not in PENDING status")
    @ApiResponse(responseCode = "404", description = "Product or items not found")
    @PatchMapping("/imports/batch")
    public ResponseEntity<List<InsyImportItemDTO>> batchUpdateStatus(
            @Parameter(description = "Batch update configuration") @Valid @RequestBody InsyBatchImportRequestDTO request
    ) {
        return ResponseEntity.ok(importService.batchImport(request));
    }

    // ========================================
    // MOCK ENDPOINTS - For Testing
    // ========================================

    @Operation(summary = "[MOCK] Create test imports",
            description = "Creates mock import items for testing. Use this to populate the import queue without real InSy connection.")
    @ApiResponse(responseCode = "201", description = "Mock data created successfully")
    @PostMapping("/mock/imports")
    public ResponseEntity<Map<String, Object>> createMockImports(
            @Parameter(description = "Number of mock items to create") @RequestParam(defaultValue = "5") int count
    ) {
        List<InsyImportPushDTO> mockItems = generateMockItems(count);
        var results = importService.receiveMultipleFromInsy(mockItems);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Mock data created",
                "created", results.size(),
                "items", importService.getAllPending()
        ));
    }

    // ========================================
    // PRIVATE HELPER - Mock Data Generation
    // ========================================

    /**
     * Generiert Mock-Daten mit FESTEN InSy-IDs
     *
     * InSy-IDs 900001-900010 sind reserviert fuer Mock-Daten.
     * Bei erneutem Aufruf werden existierende PENDING Items aktualisiert statt dupliziert.
     */
    private List<InsyImportPushDTO> generateMockItems(int count) {
        // Mock-Daten: [Name, Description, Location, Owner, InvPrefix, FESTE InSy-ID]
        Object[][] mockData = {
                {"Meta Quest 3", "VR-Brille mit Touch-Controllern, 128GB", "F01.402", "Christian Haas", "VR", 900001L},
                {"Meta Quest Pro", "High-End VR-Brille fuer professionelle Anwendungen", "F01.402", "Christian Haas", "VR", 900002L},
                {"Sony A7 IV", "Vollformat-Kamera, 33 Megapixel", "F01.310", "Christian Haas", "CAM", 900003L},
                {"Canon EOS R6 Mark II", "Spiegellose Vollformatkamera", "F01.310", "Christian Haas", "CAM", 900004L},
                {"DJI Mavic 3", "Drohne mit Hasselblad-Kamera", "F01.402", "IT-Labor", "DRONE", 900005L},
                {"MacBook Pro 16", "Apple M3 Pro, 36GB RAM", "F01.201", "IT-Labor", "MAC", 900006L},
                {"Rode NT1", "Kondensatormikrofon fuer Studioaufnahmen", "F01.310", "Christian Haas", "MIC", 900007L},
                {"Zoom H6", "Mobiler Audio-Recorder, 6 Kanaele", "F01.310", "Christian Haas", "REC", 900008L},
                {"Valve Index", "VR-Headset mit Knuckles-Controllern", "F01.402", "Christian Haas", "VR", 900009L},
                {"Insta360 X3", "360-Grad Kamera", "F01.310", "Christian Haas", "CAM", 900010L}
        };

        List<InsyImportPushDTO> items = new java.util.ArrayList<>();

        for (int i = 0; i < count && i < mockData.length; i++) {
            Object[] data = mockData[i];

            items.add(InsyImportPushDTO.builder()
                    .insyId((Long) data[5])
                    .name((String) data[0])
                    .description((String) data[1])
                    .location((String) data[2])
                    .owner((String) data[3])
                    .invNumber(data[4] + "-" + String.format("%03d", i + 1))
                    .build());
        }

        return items;
    }
}