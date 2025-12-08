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

    /**
     * Construct a new ItemService.
     *
     * @param itemRepository    repository for items
     * @param productRepository repository for products
     */
    public ItemService(ItemRepository itemRepository, ProductRepository productRepository) {
        this.itemRepository = itemRepository;
        this.productRepository = productRepository;
    }

    /**
     * Alle aktiven Items abrufen.
     *
     * @return list of active items
     */
    public List<Item> getAllItems() {
        return itemRepository.findAllActive();
    }

    /**
     * Item per ID abrufen.
     *
     * @param id item id
     * @return item
     */
    public Item getItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item nicht gefunden: " + id));
    }

    /**
     * Item per Inventarnummer abrufen.
     *
     * @param invNumber inventory number
     * @return item
     */
    public Item getItemByInvNumber(String invNumber) {
        return itemRepository.findByInvNumber(invNumber)
                .orElseThrow(() -> new RuntimeException("Item nicht gefunden: " + invNumber));
    }

    /**
     * Items eines Products abrufen.
     *
     * @param productId product id
     * @return items for the product
     */
    public List<Item> getItemsByProduct(Long productId) {
        return itemRepository.findByProductId(productId);
    }

    /**
     * Anzahl Items eines Products.
     *
     * @param productId product id
     * @return number of items
     */
    public Long countItemsByProduct(Long productId) {
        return itemRepository.countByProductId(productId);
    }

    /**
     * Neues Item erstellen.
     *
     * @param invNumber inventory number
     * @param owner     owner name
     * @param productId product id
     * @return created item
     */
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

    /**
     * Erstellt `count` gleichartige Items mit fortlaufender Inventarnummer.
     * Beginnt bei [Prefix]-001, überspringt bereits vergebene Nummern
     * und legt nur freie Nummern neu an, bis `count` erreicht ist.
     *
     * @param invNumberPrefix Der Teil vor der Nummer (z.B. "VR")
     * @param owner           Der Besitzer (optional)
     * @param productId       Die ID des Produkts
     * @param count           Anzahl der zu erstellenden Items
     * @return Liste der erstellten Items
     */
    public List<Item> createItemSet(String invNumberPrefix, String owner, Long productId, int count) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product nicht gefunden: " + productId));

        List<Item> createdItems = new java.util.ArrayList<>();
        int itemsCreated = 0;
        int currentNumber = 1;

        // Schleife läuft so lange, bis wir die gewünschte Anzahl (count) erstellt haben
        while (itemsCreated < count) {
            // Formatierung: Prefix + 3-stellige Zahl (z.B. "VR-001")
            String candidateInvNumber = invNumberPrefix + "-" + String.format("%03d", currentNumber);

            // Prüfen, ob diese Nummer schon vergeben ist
            if (itemRepository.findByInvNumber(candidateInvNumber).isEmpty()) {
                // Nummer ist frei -> Item erstellen
                Item item = new Item(candidateInvNumber, owner, product);
                createdItems.add(itemRepository.save(item));
                itemsCreated++;
            }

            // Zähler für die Nummerierung immer erhöhen, egal ob Treffer oder nicht
            currentNumber++;
        }

        return createdItems;
    }

    /**
     * Item aktualisieren.
     *
     * @param id        item id
     * @param invNumber new inventory number
     * @param owner     new owner
     * @return updated item
     */
    public Item updateItem(Long id, String invNumber, String owner) {
        Item item = getItemById(id);

        if (!item.getInvNumber().equals(invNumber) && itemRepository.findByInvNumber(invNumber).isPresent()) {
            throw new RuntimeException("Inventarnummer existiert bereits: " + invNumber);
        }

        item.setInvNumber(invNumber);
        item.setOwner(owner);
        return itemRepository.save(item);
    }

    /**
     * Item löschen (Soft-Delete).
     *
     * @param id item id
     */
    public void deleteItem(Long id) {
        Item item = getItemById(id);
        item.softDelete();
        itemRepository.save(item);
    }

    /**
     * Prüfe ob Item verfügbar ist.
     *
     * @param itemId item id
     * @return true if available
     */
    public boolean isItemAvailable(Long itemId) {
        Item item = getItemById(itemId);
        return item.isAvailable();
    }

    /**
     * Prüfe Verfügbarkeit für Zeitraum.
     *
     * @param itemId    item id
     * @param startDate start date/time
     * @param endDate   end date/time
     * @return true if available for the period
     */
    public boolean isItemAvailableForPeriod(Long itemId, LocalDateTime startDate, LocalDateTime endDate) {
        Item item = getItemById(itemId);
        return item.isAvailableForPeriod(startDate, endDate);
    }
}