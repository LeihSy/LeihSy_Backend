package com.hse.leihsy.model.dto;

import com.hse.leihsy.model.entity.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TransactionDTO {
    private Long id;
    private Long bookingId;
    private String token;
    private TransactionType transactionType;
    private LocalDateTime expiresAt;
    private Long createdByUserId;
    private LocalDateTime createdAt;
}