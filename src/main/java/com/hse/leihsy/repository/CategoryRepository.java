package com.hse.leihsy.repository;

import com.hse.leihsy.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c WHERE c.deletedAt IS NULL")
    List<Category> findAllActive();

    Optional<Category> findByNameIgnoreCase(String name);
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId AND p.deletedAt IS NULL")
    Long countProductsByCategoryId(@Param("categoryId") Long categoryId);
}