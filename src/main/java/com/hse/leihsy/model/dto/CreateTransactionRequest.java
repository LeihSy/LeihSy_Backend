package com.hse.leihsy.model.dto;

import com.hse.leihsy.model.entity.TransactionType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateTransactionRequest {
    private TransactionType transactionType;
}