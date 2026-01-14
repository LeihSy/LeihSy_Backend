package com.hse.leihsy.model.dto;
import java.util.List;
import java.time.LocalDateTime;
import lombok.*;

/**
 * DTO für Category - enthält nur die Kategorie-Attribute ohne die verknüpften Produkte
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {

    private Long id;
    private String name;
    private String icon;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long deviceCount;
    private List<String> availableLocations;
    public CategoryDTO(Long id, String name, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


}

