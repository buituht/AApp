package com.example.myapplication;

import java.io.Serializable;

public class Voucher implements Serializable {
    private String id;
    private String code;
    private String description;
    private long discountValue;
    private String type; // "PERCENT" or "FIXED"
    private long minOrderAmount;
    private long startDate;
    private long expiryDate;

    public Voucher() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getDiscountValue() { return discountValue; }
    public void setDiscountValue(long discountValue) { this.discountValue = discountValue; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public long getMinOrderAmount() { return minOrderAmount; }
    public void setMinOrderAmount(long minOrderAmount) { this.minOrderAmount = minOrderAmount; }

    public long getStartDate() { return startDate; }
    public void setStartDate(long startDate) { this.startDate = startDate; }

    public long getExpiryDate() { return expiryDate; }
    public void setExpiryDate(long expiryDate) { this.expiryDate = expiryDate; }
}
