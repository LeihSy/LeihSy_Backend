package com.hse.leihsy.model.entity;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

/**
 * Composite Primary Key für ProductSet
 * Benötigt für JPA @IdClass Annotation
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
public class ProductSetId implements Serializable {

    private Long parentProduct;
    private Long childProduct;
}