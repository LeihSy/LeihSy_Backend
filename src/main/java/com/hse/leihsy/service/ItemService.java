package com.hse.leihsy.service;

import com.hse.leihsy.model.entity.Booking;
import com.hse.leihsy.model.entity.Item;
import com.hse.leihsy.model.entity.Product;
import com.hse.leihsy.model.entity.User;
import com.hse.leihsy.repository.BookingRepository;
import com.hse.leihsy.repository.ItemRepository;
import com.hse.leihsy.repository.ProductRepository;
import com.hse.leihsy.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // for Lombok Logging

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public List<Item> getAllItems() {
        return itemRepository.findAllActive();
    }

    public List<Item> getAllDeletedItems() {
        return itemRepository.findAllDeleted();
    }

    public Item getItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item nicht gefunden: " + id));
    }

    public Item getItemByInvNumber(String invNumber) {
        return itemRepository.findByInvNumber(invNumber)
                .orElseThrow(() -> new RuntimeException("Item nicht gefunden: " + invNumber));
    }

    public List<Item> getItemsByProductId(Long productId) {
        return itemRepository.findByProductId(productId);
    }

    public List<Item> getItemsByLender(Long lenderId) { return itemRepository.findByLenderId(lenderId); }

    public Long countItemsByProduct(Long productId) {
        return itemRepository.countByProductId(productId);
    }

    public Item createItem(String invNumber, String owner, Long productId, Long lenderId) {
        if (itemRepository.findByInvNumber(invNumber).isPresent()) {
            throw new RuntimeException("Inventarnummer existiert bereits: " + invNumber);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product nicht gefunden: " + productId));

        User lender = null;
        if (lenderId != null) {
            lender = userRepository.findById(lenderId)
                    .orElseThrow(() -> new RuntimeException("Verleiher nicht gefunden: " + lenderId));
        }

        Item item = Item.builder()
                .invNumber(invNumber)
                .owner(owner)
                .lender(lender)
                .product(product)
                .build();
        return itemRepository.save(item);
    }

    public List<Item> createItemSet(String invNumberPrefix, String owner, Long productId, Long lenderId, int count) {
        log.info("Creating item set. Prefix: {}, Count: {}, LenderId: {}", invNumberPrefix, count, lenderId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product nicht gefunden: " + productId));

        User lender = null;
        if (lenderId != null) {
            lender = userRepository.findById(lenderId)
                    .orElseThrow(() -> new RuntimeException("Verleiher nicht gefunden: " + lenderId));
        }

        List<Item> items = new java.util.ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String invNumber = invNumberPrefix + "-" + String.format("%03d", i);

            if (itemRepository.findByInvNumber(invNumber).isPresent()) {
                continue;
            }

            Item item = Item.builder()
                    .invNumber(invNumber)
                    .owner(owner)
                    .lender(lender)
                    .product(product)
                    .build();
            items.add(itemRepository.save(item));
        }
        return items;
    }

    public Item updateItem(Long id, String invNumber, String owner, Long lenderId) {
        Item item = getItemById(id);

        if (!item.getInvNumber().equals(invNumber) && itemRepository.findByInvNumber(invNumber).isPresent()) {
            throw new RuntimeException("Inventarnummer existiert bereits: " + invNumber);
        }

        item.setInvNumber(invNumber);
        item.setOwner(owner);

        if (lenderId != null) {
            User lender = userRepository.findById(lenderId)
                    .orElseThrow(() -> new RuntimeException("Verleiher nicht gefunden: " + lenderId));
            item.setLender(lender);
        } else {
            item.setLender(null);
        }

        return itemRepository.save(item);
    }

    public void deleteItem(Long id) {
        log.info("Deleting item {}", id);
        Item item = getItemById(id);
        item.softDelete();
        itemRepository.save(item);
    }

    public boolean isItemAvailable(Long itemId) {
        Item item = getItemById(itemId);
        return item.isAvailable();
    }

    public boolean isItemAvailableForPeriod(Long itemId, LocalDateTime startDate, LocalDateTime endDate) {
        Item item = getItemById(itemId);
        return item.isAvailableForPeriod(startDate, endDate);
    }

    public List<Booking> getBookingsByItemId(Long itemId) {
        // Prüfen, ob Item existiert
        getItemById(itemId);
        return bookingRepository.findByItemId(itemId);
    }
    public Item updateRelatedItems(Long itemId, List<Long> relatedItemIds) {
        Item item = getItemById(itemId);
        
        List<Item> relations = itemRepository.findAllById(relatedItemIds);
        item.setRelatedItems(relations);
        
        return itemRepository.save(item);
    }

}