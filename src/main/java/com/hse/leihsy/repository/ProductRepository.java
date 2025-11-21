package com.hse.leihsy.repository;

import com.hse.leihsy.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Alle aktiven Products
    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL")
    List<Product> findAllActive();

    // Products nach Kategorie
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.deletedAt IS NULL")
    List<Product> findByCategoryId(@Param("categoryId") Long categoryId);

    // Products nach Location
    @Query("SELECT p FROM Product p WHERE p.location.id = :locationId AND p.deletedAt IS NULL")
    List<Product> findByLocationId(@Param("locationId") Long locationId);

    // Products eines Verleihers
    @Query("SELECT p FROM Product p WHERE p.lender.id = :lenderId AND p.deletedAt IS NULL")
    List<Product> findByLenderId(@Param("lenderId") Long lenderId);

    // Suche nach Name (case-insensitive, contains)
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) AND p.deletedAt IS NULL")
    List<Product> searchByName(@Param("search") String search);

    // Volltext-Suche (Name + Description)
    @Query("SELECT p FROM Product p WHERE " +
            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND p.deletedAt IS NULL")
    List<Product> fullTextSearch(@Param("search") String search);
}