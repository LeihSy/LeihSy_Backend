package com.hse.leihsy.controller;

import com.hse.leihsy.mapper.ItemMapper;
import com.hse.leihsy.model.dto.ItemCreateRequestDTO;
import com.hse.leihsy.model.dto.ItemDTO;
import com.hse.leihsy.model.entity.Item;
import com.hse.leihsy.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/items", produces = "application/json")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final ItemMapper itemMapper;

    // Alle Items abrufen
    @Operation(summary = "Get all items", description = "Returns a list of all items")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Items retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<List<ItemDTO>> getAllItems() {
        List<Item> items = itemService.getAllItems();
        return ResponseEntity.ok(itemMapper.toDTOList(items));
    }

    // Item anhand der ID abrufen
    @Operation(summary = "Get item by ID", description = "Returns an item with the matching ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item found"),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ItemDTO> getItemById(
            @Parameter(description = "ID of the item to retrieve") @PathVariable Long id) {
        Item item = itemService.getItemById(id);
        return ResponseEntity.ok(itemMapper.toDTO(item));
    }

    // Filter nach Produkt
    @Operation(summary = "Get items by product", description = "Returns a list of items filtered by product ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Items retrieved successfully")
    })
    @GetMapping("/products/{productId}")
    public ResponseEntity<List<ItemDTO>> getItemsByProduct(
            @Parameter(description = "ID of the product") @PathVariable Long productId) {
        List<Item> items = itemService.getItemsByProduct(productId);
        return ResponseEntity.ok(itemMapper.toDTOList(items));
    }

    // Filter nach Verleiher
    @Operation(summary = "Get items by lender", description = "Returns a list of items assigned to a specific lender")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Items retrieved successfully")
    })
    @GetMapping("/lender/{lenderId}")
    public ResponseEntity<List<ItemDTO>> getItemsByLender(
            @Parameter(description = "ID of the lender (User ID)") @PathVariable Long lenderId) {
        List<Item> items = itemService.getItemsByLender(lenderId);
        return ResponseEntity.ok(itemMapper.toDTOList(items));
    }

    // Suche nach Inventarnummer
    @Operation(summary = "Get item by inventory number", description = "Returns an item by its inventory number")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item found"),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    @GetMapping("/invnumber/{invNumber}")
    public ResponseEntity<ItemDTO> getItemByInvNumber(
            @Parameter(description = "Inventory number of the item") @PathVariable String invNumber) {
        Item item = itemService.getItemByInvNumber(invNumber);
        return ResponseEntity.ok(itemMapper.toDTO(item));
    }

    // Neues Item erstellen
    @Operation(summary = "Create a new item", description = "Creates a new item Lender can be assigned via lenderId.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping
    public ResponseEntity<ItemDTO> createItem(
            @Parameter(description = "Item creation data") @Valid @RequestBody ItemCreateRequestDTO request) {
        Item item = itemService.createItem(
                request.getInvNumber(),
                request.getOwner(),
                request.getProductId(),
                request.getLenderId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(itemMapper.toDTO(item));
    }

    // Item aktualisieren
    @Operation(summary = "Update an item", description = "Updates an existing item by ID. Assigns new lender if lenderId is provided.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item updated successfully"),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ItemDTO> updateItem(
            @Parameter(description = "ID of the item to update") @PathVariable Long id,
            @Parameter(description = "Updated item data") @Valid @RequestBody ItemCreateRequestDTO request) {
        Item item = itemService.updateItem(
                id,
                request.getInvNumber(),
                request.getOwner(),
                request.getLenderId()
        );
        return ResponseEntity.ok(itemMapper.toDTO(item));
    }

    // Item löschen
    @Operation(summary = "Delete an item", description = "Deletes an item by ID (soft delete)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(
            @Parameter(description = "ID of the item to delete") @PathVariable Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
}