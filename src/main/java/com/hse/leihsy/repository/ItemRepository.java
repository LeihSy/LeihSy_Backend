package com.hse.leihsy.repository;

import com.hse.leihsy.model.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    // Alle aktiven Items
    @Query("SELECT i FROM Item i WHERE i.deletedAt IS NULL")
    List<Item> findAllActive();

    // Items eines Products
    @Query("SELECT i FROM Item i WHERE i.product.id = :productId AND i.deletedAt IS NULL")
    List<Item> findByProductId(@Param("productId") Long productId);

    // Item nach Inventarnummer
    Optional<Item> findByInvNumber(String invNumber);

    // Zaehle verfuegbare Items eines Products
    @Query("SELECT COUNT(i) FROM Item i WHERE i.product.id = :productId AND i.deletedAt IS NULL")
    Long countByProductId(@Param("productId") Long productId);
}