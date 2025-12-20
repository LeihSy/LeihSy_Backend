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

import java.util.List;

@RestController
@RequestMapping(value = "/api/items", produces = "application/json")
@CrossOrigin(origins = "http://localhost:4200")
public class ItemController {

    private final ItemService itemService;
    private final ItemMapper itemMapper;

    public ItemController(ItemService itemService, ItemMapper itemMapper) {
        this.itemService = itemService;
        this.itemMapper = itemMapper;
    }

    @Operation(summary = "Get all items", description = "Returns a list of all items")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Items retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<List<ItemDTO>> getAllItems() {
        List<Item> items = itemService.getAllItems();
        return ResponseEntity.ok(itemMapper.toDTOList(items));
    }

    @Operation(summary = "Get all deleted items", description = "Returns a list of all deleted items (where deletedAt is not null)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deleted items retrieved successfully")
    })
    @GetMapping("/deleted")
    public ResponseEntity<List<ItemDTO>> getAllDeletedItems() {
        List<Item> items = itemService.getAllDeletedItems();
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

    @Operation(summary = "Update an item", description = "Updates an existing item by ID")
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