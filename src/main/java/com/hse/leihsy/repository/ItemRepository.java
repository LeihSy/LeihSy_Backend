package com.hse.leihsy.repository;

import com.hse.leihsy.model.entity.Item;
import com.hse.leihsy.model.entity.ItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    // Finde Item per Inventarnummer
    Optional<Item> findByInventoryNumber(String inventoryNumber);

    // Finde Items per Status
    List<Item> findByStatus(ItemStatus status);

    // Finde Items per Kategorie
    List<Item> findByCategoryId(Long categoryId);

    // Suche nach Name (Case-insensitive)
    List<Item> findByNameContainingIgnoreCase(String name);
}