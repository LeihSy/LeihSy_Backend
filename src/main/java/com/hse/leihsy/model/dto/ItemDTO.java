package com.hse.leihsy.model.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data // Auto-generates getters, setters, toString, etc.
@NoArgsConstructor // Generates empty constructor
@AllArgsConstructor // Generates full constructor

public class ItemDTO {

    private Long id;
    private String invNumber;
    private String owner;
    private Long productId;
    private String productName;
    private boolean available;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}