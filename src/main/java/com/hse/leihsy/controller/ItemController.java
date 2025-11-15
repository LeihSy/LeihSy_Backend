package com.hse.leihsy.controller;

import com.hse.leihsy.model.dto.ItemCreateDTO;
import com.hse.leihsy.model.dto.ItemDTO;
import com.hse.leihsy.model.entity.ItemStatus;
import com.hse.leihsy.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "http://localhost:4200")
public class ItemController {

    private final ItemService itemService;

    // Constructor Injection (statt @RequiredArgsConstructor)
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    // GET /api/items - Alle Items
    @GetMapping
    public ResponseEntity<List<ItemDTO>> getAllItems() {
        return ResponseEntity.ok(itemService.getAllItems());
    }

    // GET /api/items/{id} - Item per ID
    @GetMapping("/{id}")
    public ResponseEntity<ItemDTO> getItemById(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItemById(id));
    }

    // POST /api/items - Neues Item
    @PostMapping
    public ResponseEntity<ItemDTO> createItem(@Valid @RequestBody ItemCreateDTO createDTO) {
        ItemDTO created = itemService.createItem(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // PUT /api/items/{id} - Item aktualisieren
    @PutMapping("/{id}")
    public ResponseEntity<ItemDTO> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemCreateDTO updateDTO
    ) {
        return ResponseEntity.ok(itemService.updateItem(id, updateDTO));
    }

    // DELETE /api/items/{id} - Item löschen
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/items/search?keyword=... - Suche
    @GetMapping("/search")
    public ResponseEntity<List<ItemDTO>> searchItems(@RequestParam String keyword) {
        return ResponseEntity.ok(itemService.searchItems(keyword));
    }

    // GET /api/items/status/{status} - Items per Status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ItemDTO>> getItemsByStatus(@PathVariable ItemStatus status) {
        return ResponseEntity.ok(itemService.getItemsByStatus(status));
    }
}