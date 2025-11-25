package com.hse.leihsy.controller;

import com.hse.leihsy.model.dto.ItemDTO;
import com.hse.leihsy.model.dto.ItemCreateDTO;
import com.hse.leihsy.model.entity.Item;
import com.hse.leihsy.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "http://localhost:4200")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public ResponseEntity<List<ItemDTO>> getAllItems() {
        List<Item> items = itemService.getAllItems();
        List<ItemDTO> dtos = items.stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDTO> getItemById(@PathVariable Long id) {
        Item item = itemService.getItemById(id);
        return ResponseEntity.ok(convertToDTO(item));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ItemDTO>> getItemsByProduct(@PathVariable Long productId) {
        List<Item> items = itemService.getItemsByProduct(productId);
        List<ItemDTO> dtos = items.stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/invnumber/{invNumber}")
    public ResponseEntity<ItemDTO> getItemByInvNumber(@PathVariable String invNumber) {
        Item item = itemService.getItemByInvNumber(invNumber);
        return ResponseEntity.ok(convertToDTO(item));
    }

    @PostMapping
    public ResponseEntity<ItemDTO> createItem(@Valid @RequestBody ItemCreateDTO createDTO) {
        Item item = itemService.createItem(
                createDTO.getInvNumber(),
                createDTO.getOwner(),
                createDTO.getProductId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(item));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemDTO> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemCreateDTO updateDTO) {

        Item item = itemService.updateItem(
                id,
                updateDTO.getInvNumber(),
                updateDTO.getOwner()
        );
        return ResponseEntity.ok(convertToDTO(item));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
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