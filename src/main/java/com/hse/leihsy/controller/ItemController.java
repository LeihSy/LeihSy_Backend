package com.hse.leihsy.controller;

import com.hse.leihsy.mapper.ItemMapper;
import com.hse.leihsy.model.dto.ItemDTO;
import com.hse.leihsy.model.dto.ItemCreateDTO;
import com.hse.leihsy.model.entity.Item;
import com.hse.leihsy.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final ItemMapper itemMapper;

    @GetMapping
    public ResponseEntity<List<ItemDTO>> getAllItems() {
        List<Item> items = itemService.getAllItems();
        return ResponseEntity.ok(itemMapper.toDTOs(items));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDTO> getItemById(@PathVariable Long id) {
        Item item = itemService.getItemById(id);
        return ResponseEntity.ok(itemMapper.toDTO(item));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ItemDTO>> getItemsByProduct(@PathVariable Long productId) {
        List<Item> items = itemService.getItemsByProduct(productId);
        return ResponseEntity.ok(itemMapper.toDTOs(items));
    }

    @GetMapping("/invnumber/{invNumber}")
    public ResponseEntity<ItemDTO> getItemByInvNumber(@PathVariable String invNumber) {
        Item item = itemService.getItemByInvNumber(invNumber);
        return ResponseEntity.ok(itemMapper.toDTO(item));
    }

    @PostMapping
    public ResponseEntity<ItemDTO> createItem(@Valid @RequestBody ItemCreateDTO createDTO) {
        Item item = itemService.createItem(
                createDTO.getInvNumber(),
                createDTO.getOwner(),
                createDTO.getProductId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(itemMapper.toDTO(item));
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
        return ResponseEntity.ok(itemMapper.toDTO(item));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
}