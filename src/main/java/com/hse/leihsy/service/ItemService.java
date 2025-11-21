package com.hse.leihsy.service;

import com.hse.leihsy.model.entity.Item;
import com.hse.leihsy.model.entity.Product;
import com.hse.leihsy.repository.ItemRepository;
import com.hse.leihsy.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ItemService {

    private final ItemRepository itemRepository;
    private final ProductRepository productRepository;

    public ItemService(ItemRepository itemRepository, ProductRepository productRepository) {
        this.itemRepository = itemRepository;
        this.productRepository = productRepository;
    }

    // Alle aktiven Items abrufen
    public List<Item> getAllItems() {
        return itemRepository.findAllActive();
    }

    // Item per ID abrufen
    public Item getItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item nicht gefunden: " + id));
    }

    // Item per Inventarnummer abrufen
    public Item getItemByInvNumber(String invNumber) {
        return itemRepository.findByInvNumber(invNumber)
                .orElseThrow(() -> new RuntimeException("Item nicht gefunden: " + invNumber));
    }

    // Items eines Products abrufen
    public List<Item> getItemsByProduct(Long productId) {
        return itemRepository.findByProductId(productId);
    }

    // Anzahl Items eines Products
    public Long countItemsByProduct(Long productId) {
        return itemRepository.countByProductId(productId);
    }

    // Neues Item erstellen
    public Item createItem(String invNumber, String owner, Long productId) {
        // Prüfe ob Inventarnummer bereits existiert
        if (itemRepository.findByInvNumber(invNumber).isPresent()) {
            throw new RuntimeException("Inventarnummer existiert bereits: " + invNumber);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product nicht gefunden: " + productId));

        Item item = new Item(invNumber, owner, product);
        return itemRepository.save(item);
    }

    // Mehrere Items auf einmal erstellen
    public List<Item> createItemSet(String invNumberPrefix, String owner, Long productId, int count) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product nicht gefunden: " + productId));

        List<Item> items = new java.util.ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String invNumber = invNumberPrefix + "-" + String.format("%03d", i);

            if (itemRepository.findByInvNumber(invNumber).isPresent()) {
                continue;
            }

            Item item = new Item(invNumber, owner, product);
            items.add(itemRepository.save(item));
        }
        return items;
    }

    // Item aktualisieren
    public Item updateItem(Long id, String invNumber, String owner) {
        Item item = getItemById(id);

        if (!item.getInvNumber().equals(invNumber)) {
            if (itemRepository.findByInvNumber(invNumber).isPresent()) {
                throw new RuntimeException("Inventarnummer existiert bereits: " + invNumber);
            }
        }

        item.setInvNumber(invNumber);
        item.setOwner(owner);
        return itemRepository.save(item);
    }

    // Item löschen (Soft-Delete)
    public void deleteItem(Long id) {
        Item item = getItemById(id);
        item.softDelete();
        itemRepository.save(item);
    }

    // Prüfe ob Item verfügbar ist
    public boolean isItemAvailable(Long itemId) {
        Item item = getItemById(itemId);
        return item.isAvailable();
    }

    // Prüfe Verfügbarkeit für Zeitraum
    public boolean isItemAvailableForPeriod(Long itemId, LocalDateTime startDate, LocalDateTime endDate) {
        Item item = getItemById(itemId);
        return item.isAvailableForPeriod(startDate, endDate);
    }
}