package com.hse.leihsy.model.dto;

import java.time.LocalDateTime;
import lombok.*;

/**
 * DTO für Category - enthält nur die Kategorie-Attribute ohne die verknüpften Produkte
 */
@Data
@NoArgsConstructor
@Builder
public class CategoryDTO {

    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CategoryDTO(Long id, String name, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


}

