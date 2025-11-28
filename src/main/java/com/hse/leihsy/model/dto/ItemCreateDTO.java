package com.hse.leihsy.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data // Auto-generates getters, setters, toString, etc.
@NoArgsConstructor // Generates empty constructor
@AllArgsConstructor // Generates full constructor

public class ItemCreateDTO {

    @NotBlank(message = "Inventarnummer ist erforderlich")
    private String invNumber;

    private String owner;

    @NotNull(message = "Product ist erforderlich")
    private Long productId;
}