package com.hse.leihsy.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class ItemCreateRequestDTO {

    @NotBlank(message = "Inventarnummer ist erforderlich")
    private String invNumber;

    private String owner;

    @NotNull(message = "Product ist erforderlich")
    private Long productId;

    private Long lenderId;
}