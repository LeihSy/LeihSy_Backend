package com.hse.leihsy.model.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite Primary Key für ProductSet
 * Benötigt für JPA @IdClass Annotation
 */
public class ProductSetId implements Serializable {

    private Long parentProduct;
    private Long childProduct;

    // Constructors

    public ProductSetId() {
    }

    public ProductSetId(Long parentProduct, Long childProduct) {
        this.parentProduct = parentProduct;
        this.childProduct = childProduct;
    }

    // equals and hashCode

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductSetId that = (ProductSetId) o;
        return Objects.equals(parentProduct, that.parentProduct) &&
                Objects.equals(childProduct, that.childProduct);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentProduct, childProduct);
    }

    // Getters and Setters

    public Long getParentProduct() {
        return parentProduct;
    }

    public void setParentProduct(Long parentProduct) {
        this.parentProduct = parentProduct;
    }

    public Long getChildProduct() {
        return childProduct;
    }

    public void setChildProduct(Long childProduct) {
        this.childProduct = childProduct;
    }
}