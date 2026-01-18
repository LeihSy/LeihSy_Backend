package com.hse.leihsy.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;
import java.util.List; 

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    private Long availableItems;
    private Long totalItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ProductRelationDTO> relatedItems;
}