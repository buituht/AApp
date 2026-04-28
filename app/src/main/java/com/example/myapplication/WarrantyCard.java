package com.example.myapplication;

import java.io.Serializable;

public class WarrantyCard implements Serializable {
    private String warrantyId;
    private String orderId;
    private String productId;
    private String productName;
    private String userEmail;
    private String username;
    private long activationDate;
    private long expiryDate;
    private String status; // "Active", "Expired", "Claimed"
    private String warrantyPeriod;

    public WarrantyCard() {
        // Required for Firestore
    }

    public WarrantyCard(String warrantyId, String orderId, String productId, String productName, String userEmail, String username, long activationDate, long expiryDate, String status, String warrantyPeriod) {
        this.warrantyId = warrantyId;
        this.orderId = orderId;
        this.productId = productId;
        this.productName = productName;
        this.userEmail = userEmail;
        this.username = username;
        this.activationDate = activationDate;
        this.expiryDate = expiryDate;
        this.status = status;
        this.warrantyPeriod = warrantyPeriod;
    }

    // Getters and Setters
    public String getWarrantyId() { return warrantyId; }
    public void setWarrantyId(String warrantyId) { this.warrantyId = warrantyId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public long getActivationDate() { return activationDate; }
    public void setActivationDate(long activationDate) { this.activationDate = activationDate; }
    public long getExpiryDate() { return expiryDate; }
    public void setExpiryDate(long expiryDate) { this.expiryDate = expiryDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getWarrantyPeriod() { return warrantyPeriod; }
    public void setWarrantyPeriod(String warrantyPeriod) { this.warrantyPeriod = warrantyPeriod; }
}
