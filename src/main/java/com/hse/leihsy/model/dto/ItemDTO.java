package com.hse.leihsy.model.dto;

import java.time.LocalDateTime;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
public class ItemDTO {

    private Long id;
    private String invNumber;
    private String owner;
    private Long lenderId;
    private String lenderName;
    private Long productId;
    private String productName;
    private boolean available;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}