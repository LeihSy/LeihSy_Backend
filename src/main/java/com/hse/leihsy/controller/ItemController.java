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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping(value = "/api/items", produces = "application/json")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
@Tag(name = "item-controller", description = "APIs for managing physical items (exemplars)")
public class ItemController {

    private final ItemService itemService;
    private final ItemMapper itemMapper;

    // ========================================
    // GET ENDPOINTS
    // ========================================

    @Operation(summary = "Get all items", description = "Returns a list of all items. Use ?deleted=true to include deleted items.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Items retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<List<ItemDTO>> getAllItems(
            @Parameter(description = "Include deleted items") @RequestParam(required = false) Boolean deleted
    ) {
        List<Item> items;
        if (Boolean.TRUE.equals(deleted)) {
            items = itemService.getAllDeletedItems();
        } else {
            items = itemService.getAllItems();
        }
        return ResponseEntity.ok(itemMapper.toDTOList(items));
    }

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

    // ========================================
    // POST ENDPOINT
    // ========================================

    @Operation(summary = "Create a new item", description = "Creates a new item with the given data")
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

    // ========================================
    // PUT ENDPOINT
    // ========================================

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

    // ========================================
    // DELETE ENDPOINT
    // ========================================

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