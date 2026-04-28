package com.example.myapplication;

import java.io.Serializable;
import java.util.List;

public class Review implements Serializable {
    private String reviewId;
    private String productId;
    private String userEmail;
    private String userName;
    private float rating;
    private String comment;
    private long timestamp;
    private List<String> images; // Thêm trường danh sách hình ảnh

    public Review() {
        // Required for Firestore
    }

    public Review(String productId, String userEmail, String userName, float rating, String comment, long timestamp, List<String> images) {
        this.productId = productId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.rating = rating;
        this.comment = comment;
        this.timestamp = timestamp;
        this.images = images;
    }

    // Getters and Setters
    public String getReviewId() { return reviewId; }
    public void setReviewId(String reviewId) { this.reviewId = reviewId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
}
