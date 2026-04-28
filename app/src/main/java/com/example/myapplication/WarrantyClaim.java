package com.example.myapplication;

import java.io.Serializable;
import java.util.List;

public class WarrantyClaim implements Serializable {
    private String claimId;
    private String warrantyId;
    private String productId;
    private String productName;
    private String userEmail;
    private String description;
    private List<String> images;
    private String status; // "Pending", "Processing", "Resolved", "Rejected"
    private long timestamp;

    public WarrantyClaim() {
        // Required for Firestore
    }

    public WarrantyClaim(String claimId, String warrantyId, String productId, String productName, String userEmail, String description, List<String> images, String status, long timestamp) {
        this.claimId = claimId;
        this.warrantyId = warrantyId;
        this.productId = productId;
        this.productName = productName;
        this.userEmail = userEmail;
        this.description = description;
        this.images = images;
        this.status = status;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getClaimId() { return claimId; }
    public void setClaimId(String claimId) { this.claimId = claimId; }
    public String getWarrantyId() { return warrantyId; }
    public void setWarrantyId(String warrantyId) { this.warrantyId = warrantyId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
