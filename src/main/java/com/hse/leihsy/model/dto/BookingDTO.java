package com.hse.leihsy.model.dto;

import java.time.LocalDateTime;

public class BookingDTO {

    private Long id;
    private Long userId;
    private String userName;
    private Long lenderId;
    private String lenderName;
    private Long itemId;
    private String itemInvNumber;
    private Long productId;
    private String productName;
    private String message;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String proposedPickups;
    private Long proposalById;
    private String proposalByName;
    private LocalDateTime confirmedPickup;
    private LocalDateTime distributionDate;
    private LocalDateTime returnDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BookingDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getLenderId() {
        return lenderId;
    }

    public void setLenderId(Long lenderId) {
        this.lenderId = lenderId;
    }

    public String getLenderName() {
        return lenderName;
    }

    public void setLenderName(String lenderName) {
        this.lenderName = lenderName;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getItemInvNumber() {
        return itemInvNumber;
    }

    public void setItemInvNumber(String itemInvNumber) {
        this.itemInvNumber = itemInvNumber;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public String getProposedPickups() {
        return proposedPickups;
    }

    public void setProposedPickups(String proposedPickups) {
        this.proposedPickups = proposedPickups;
    }

    public Long getProposalById() {
        return proposalById;
    }

    public void setProposalById(Long proposalById) {
        this.proposalById = proposalById;
    }

    public String getProposalByName() {
        return proposalByName;
    }

    public void setProposalByName(String proposalByName) {
        this.proposalByName = proposalByName;
    }

    public LocalDateTime getConfirmedPickup() {
        return confirmedPickup;
    }

    public void setConfirmedPickup(LocalDateTime confirmedPickup) {
        this.confirmedPickup = confirmedPickup;
    }

    public LocalDateTime getDistributionDate() {
        return distributionDate;
    }

    public void setDistributionDate(LocalDateTime distributionDate) {
        this.distributionDate = distributionDate;
    }

    public LocalDateTime getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDateTime returnDate) {
        this.returnDate = returnDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}