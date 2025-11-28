package com.hse.leihsy.model.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data // Auto-generates getters, setters, toString, etc.
@NoArgsConstructor // Generates empty constructor
@AllArgsConstructor // Generates full constructor

public class BookingDTO {

    private Long id;
    private Long userId;
    private String userName;
    private Long receiverId;
    private String receiverName;
    private Long itemId;
    private String itemInvNumber;
    private Long productId;
    private String productName;
    private String message;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime proposalPickup;
    private Long proposalById;
    private String proposalByName;
    private LocalDateTime confirmedPickup;
    private LocalDateTime distributionDate;
    private LocalDateTime returnDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}