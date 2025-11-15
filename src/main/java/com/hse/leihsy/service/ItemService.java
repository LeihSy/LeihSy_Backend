package com.hse.leihsy.service;

import com.hse.leihsy.model.dto.ItemCreateDTO;
import com.hse.leihsy.model.dto.ItemDTO;
import com.hse.leihsy.model.entity.Category;
import com.hse.leihsy.model.entity.Item;
import com.hse.leihsy.model.entity.ItemStatus;
import com.hse.leihsy.repository.CategoryRepository;
import com.hse.leihsy.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ItemService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;

    // Constructor Injection
    public ItemService(ItemRepository itemRepository, CategoryRepository categoryRepository) {
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
    }

    // Alle Items abrufen
    public List<ItemDTO> getAllItems() {
        return itemRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Item per ID abrufen
    public ItemDTO getItemById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item nicht gefunden: " + id));
        return convertToDTO(item);
    }

    // Neues Item erstellen
    public ItemDTO createItem(ItemCreateDTO createDTO) {
        // Prüfe ob Inventarnummer bereits existiert
        if (itemRepository.findByInventoryNumber(createDTO.getInventoryNumber()).isPresent()) {
            throw new RuntimeException("Inventarnummer existiert bereits: " + createDTO.getInventoryNumber());
        }

        // Lade Kategorie
        Category category = categoryRepository.findById(createDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Kategorie nicht gefunden: " + createDTO.getCategoryId()));

        // Erstelle Item
        Item item = new Item();
        item.setInventoryNumber(createDTO.getInventoryNumber());
        item.setName(createDTO.getName());
        item.setDescription(createDTO.getDescription());
        item.setCategory(category);
        item.setLocation(createDTO.getLocation());
        item.setImageUrl(createDTO.getImageUrl());
        item.setAccessories(createDTO.getAccessories());
        item.setStatus(ItemStatus.AVAILABLE);

        Item savedItem = itemRepository.save(item);
        return convertToDTO(savedItem);
    }

    // Item aktualisieren
    public ItemDTO updateItem(Long id, ItemCreateDTO updateDTO) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item nicht gefunden: " + id));

        Category category = categoryRepository.findById(updateDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Kategorie nicht gefunden: " + updateDTO.getCategoryId()));

        item.setInventoryNumber(updateDTO.getInventoryNumber());
        item.setName(updateDTO.getName());
        item.setDescription(updateDTO.getDescription());
        item.setCategory(category);
        item.setLocation(updateDTO.getLocation());
        item.setImageUrl(updateDTO.getImageUrl());
        item.setAccessories(updateDTO.getAccessories());

        Item updatedItem = itemRepository.save(item);
        return convertToDTO(updatedItem);
    }

    // Item löschen
    public void deleteItem(Long id) {
        if (!itemRepository.existsById(id)) {
            throw new RuntimeException("Item nicht gefunden: " + id);
        }
        itemRepository.deleteById(id);
    }

    // Suche Items
    public List<ItemDTO> searchItems(String keyword) {
        return itemRepository.findByNameContainingIgnoreCase(keyword)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Items per Status
    public List<ItemDTO> getItemsByStatus(ItemStatus status) {
        return itemRepository.findByStatus(status)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Helper: Entity -> DTO
    private ItemDTO convertToDTO(Item item) {
        ItemDTO dto = new ItemDTO();
        dto.setId(item.getId());
        dto.setInventoryNumber(item.getInventoryNumber());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setCategoryId(item.getCategory().getId());
        dto.setCategoryName(item.getCategory().getName());
        dto.setLocation(item.getLocation());
        dto.setImageUrl(item.getImageUrl());
        dto.setStatus(item.getStatus());
        dto.setAccessories(item.getAccessories());
        return dto;
    }
}