package com.hse.leihsy.controller;

import com.hse.leihsy.model.dto.ItemDTO;
import com.hse.leihsy.model.dto.ItemCreateDTO;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/items", produces = "application/json")
@CrossOrigin(origins = "http://localhost:4200")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }


    @Operation(summary = "Get all items", description = "Returns a list of all items")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Items retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<List<ItemDTO>> getAllItems() {
        List<Item> items = itemService.getAllItems();
        List<ItemDTO> dtos = items.stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(dtos);
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
        return ResponseEntity.ok(convertToDTO(item));
    }



    @Operation(summary = "Get items by product", description = "Returns a list of items filtered by product ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Items retrieved successfully")
    })
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ItemDTO>> getItemsByProduct(
            @Parameter(description = "ID of the product") @PathVariable Long productId) {
        List<Item> items = itemService.getItemsByProduct(productId);
        List<ItemDTO> dtos = items.stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }


    @Operation(summary = "Get item by inventory number", description = "Returns an item by its inventory number")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item found"),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    @GetMapping("/invnumber/{invNumber}")
    public ResponseEntity<ItemDTO> getItemByInvNumber(
            @Parameter(description = "Inventory number of the item") @PathVariable String invNumber) {
        Item item = itemService.getItemByInvNumber(invNumber);
        return ResponseEntity.ok(convertToDTO(item));
    }


    @Operation(summary = "Create a new item", description = "Creates a new item with the given data")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping
    public ResponseEntity<ItemDTO> createItem(
            @Parameter(description = "Data for the new item") @Valid @RequestBody ItemCreateDTO createDTO) {
        Item item = itemService.createItem(
                createDTO.getInvNumber(),
                createDTO.getOwner(),
                createDTO.getProductId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(item));
    }


    @Operation(summary = "Update an item", description = "Updates an existing item by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item updated successfully"),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ItemDTO> updateItem(
            @Parameter(description = "ID of the item to update") @PathVariable Long id,
            @Parameter(description = "Updated item data") @Valid @RequestBody ItemCreateDTO updateDTO) {

        Item item = itemService.updateItem(
                id,
                updateDTO.getInvNumber(),
                updateDTO.getOwner()
        );
        return ResponseEntity.ok(convertToDTO(item));
    }


    @Operation(summary = "Delete an item", description = "Deletes an item by ID")
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

    private ItemDTO convertToDTO(Item item) {
        ItemDTO dto = new ItemDTO();
        dto.setId(item.getId());
        dto.setInvNumber(item.getInvNumber());
        dto.setOwner(item.getOwner());
        dto.setAvailable(item.isAvailable());
        dto.setCreatedAt(item.getCreatedAt());
        dto.setUpdatedAt(item.getUpdatedAt());

        if (item.getProduct() != null) {
            dto.setProductId(item.getProduct().getId());
            dto.setProductName(item.getProduct().getName());
        }

        return dto;
    }
}