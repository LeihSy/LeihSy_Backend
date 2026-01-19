package com.hse.leihsy.model.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRelationDTO {
    private Long productId;
    private String name;    
    private String type;    // "required" oder "recommended"
}