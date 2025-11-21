package com.hse.leihsy.repository;

import com.hse.leihsy.model.entity.ProductSet;
import com.hse.leihsy.model.entity.ProductSetId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSetRepository extends JpaRepository<ProductSet, ProductSetId> {

    // Empfohlene Produkte für ein Produkt
    @Query("SELECT ps.childProduct FROM ProductSet ps WHERE ps.parentProduct.id = :productId AND ps.deletedAt IS NULL")
    List<com.hse.leihsy.model.entity.Product> findRecommendedProducts(@Param("productId") Long productId);
}