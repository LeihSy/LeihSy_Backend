package com.hse.leihsy.repository;

import com.hse.leihsy.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Alle nicht gelöschten Kategorien
    @Query("SELECT c FROM Category c WHERE c.deletedAt IS NULL")
    List<Category> findAllActive();

    // Kategorie nach Name finden
    Optional<Category> findByNameIgnoreCase(String name);
}