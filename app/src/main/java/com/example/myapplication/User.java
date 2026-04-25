package com.example.myapplication;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;

public class User implements Serializable {
    private String email;
    private String username;
    private String fullName;
    private String phoneNumber;
    private String dob;
    private String gender;
    private String address;
    private String homeAddress;
    private String companyAddress;
    private boolean termsAccepted;
    private String password;
    private String avatarUrl;
    private boolean isAdmin;
    
    private int points;
    private long totalSpent;

    public User() {
        // Required for Firestore
    }

    public User(String email, String username, String fullName, String phoneNumber, String dob, String gender, String address, String homeAddress, String companyAddress, boolean termsAccepted) {
        this.email = email;
        this.username = username;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.dob = dob;
        this.gender = gender;
        this.address = address;
        this.homeAddress = homeAddress;
        this.companyAddress = companyAddress;
        this.termsAccepted = termsAccepted;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getHomeAddress() { return homeAddress; }
    public void setHomeAddress(String homeAddress) { this.homeAddress = homeAddress; }

    public String getCompanyAddress() { return companyAddress; }
    public void setCompanyAddress(String companyAddress) { this.companyAddress = companyAddress; }

    public boolean isTermsAccepted() { return termsAccepted; }
    public void setTermsAccepted(boolean termsAccepted) { this.termsAccepted = termsAccepted; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    @PropertyName("isAdmin")
    public boolean isAdmin() { return isAdmin; }
    @PropertyName("isAdmin")
    public void setAdmin(boolean admin) { this.isAdmin = admin; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public long getTotalSpent() { return totalSpent; }
    public void setTotalSpent(long totalSpent) { this.totalSpent = totalSpent; }

    @Exclude
    public String getMembershipLevel() {
        if (totalSpent >= 50000000) return "Kim cương";
        if (totalSpent >= 20000000) return "Vàng";
        if (totalSpent >= 5000000) return "Bạc";
        return "Đồng";
    }
}
