package com.example.myapplication;

import java.io.Serializable;
import java.util.List;

public class Order implements Serializable {
    private String orderId;
    private String username;
    private String userEmail; // Thêm trường email để truy vấn chính xác
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private List<Product> items;
    private long totalPrice;
    private String status; // "Đang xử lý", "Đang giao", "Đã giao"
    private long timestamp;

    public Order() {
        // Required for Firestore
    }

    public Order(String orderId, String username, String userEmail, String receiverName, String receiverPhone, String receiverAddress, List<Product> items, long totalPrice, String status, long timestamp) {
        this.orderId = orderId;
        this.username = username;
        this.userEmail = userEmail;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.receiverAddress = receiverAddress;
        this.items = items;
        this.totalPrice = totalPrice;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }

    public String getReceiverAddress() { return receiverAddress; }
    public void setReceiverAddress(String receiverAddress) { this.receiverAddress = receiverAddress; }

    public List<Product> getItems() { return items; }
    public void setItems(List<Product> items) { this.items = items; }

    public long getTotalPrice() { return totalPrice; }
    public void setTotalPrice(long totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
