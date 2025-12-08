package com.hse.leihsy.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data // Auto-generates getters, setters, toString, etc.
@NoArgsConstructor // Generates empty constructor
@AllArgsConstructor // Generates full constructor

public class ProductDTO {

    private Long id;
    private String name;
    private String description;
    private Integer expiryDate;
    private BigDecimal price;
    private String imageUrl;
    private String accessories;
    private Long categoryId;
    private String categoryName;
    private Long locationId;
    private String locationRoomNr;
    private Long lenderId;
    private String lenderName;
    private Long availableItems;
    private Long totalItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}