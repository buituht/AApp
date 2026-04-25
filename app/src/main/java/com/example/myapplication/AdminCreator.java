package com.example.myapplication;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminCreator {
    public static void createAdminAccount() {
        User admin = new User(
            "admin@example.com",
            "admin",
            "Administrator",
            "0123456789",
            "01/01/1990",
            "Nam",
            "Hà Nội",
            "Hà Nội",
            "Hà Nội",
            true
        );
        admin.setPassword("1234567");
        admin.setAdmin(true);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Ghi đè tài khoản admin mỗi lần ứng dụng khởi chạy để đảm bảo thông tin đúng
        db.collection("users").document(admin.getEmail())
            .set(admin)
            .addOnSuccessListener(aVoid -> Log.d("AdminCreator", "Admin account created/updated successfully"))
            .addOnFailureListener(e -> Log.e("AdminCreator", "Error creating admin account", e));
    }
}
