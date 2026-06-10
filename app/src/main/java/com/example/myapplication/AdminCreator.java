package com.example.myapplication;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class AdminCreator {
    private static final String TAG = "AdminCreator";

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
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Admin account created/updated successfully");
                createDefaultCategories(); // Tạo thêm danh mục mẫu sau khi cập nhật admin
            })
            .addOnFailureListener(e -> Log.e(TAG, "Error creating admin account", e));
    }

    public static void createDefaultCategories() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // 1. Tạo các danh mục cha
        Category catLaptop = new Category("cat_laptop", "Laptop", "https://img.icons8.com/color/96/laptop.png");
        Category catPhone = new Category("cat_phone", "Điện thoại", "https://img.icons8.com/color/96/iphone.png");
        Category catAccessory = new Category("cat_accessory", "Phụ kiện", "https://img.icons8.com/color/96/usb-adapter.png");

        db.collection("categories").document(catLaptop.getId()).set(catLaptop);
        db.collection("categories").document(catPhone.getId()).set(catPhone);
        db.collection("categories").document(catAccessory.getId()).set(catAccessory);

        // 2. Tạo danh mục con cho Laptop
        saveCategory(db, new Category("sub_macbook", "Macbook", "https://img.icons8.com/color/96/mac-os--v1.png", "cat_laptop"));
        saveCategory(db, new Category("sub_dell", "Dell", "https://img.icons8.com/color/96/dell.png", "cat_laptop"));
        saveCategory(db, new Category("sub_asus", "ASUS", "https://img.icons8.com/color/96/asus.png", "cat_laptop"));
        saveCategory(db, new Category("sub_hp", "HP", "https://img.icons8.com/color/96/hp.png", "cat_laptop"));
        saveCategory(db, new Category("sub_lenovo", "Lenovo", "https://img.icons8.com/color/96/lenovo.png", "cat_laptop"));
        saveCategory(db, new Category("sub_acer", "Acer", "https://img.icons8.com/color/96/acer.png", "cat_laptop"));
        saveCategory(db, new Category("sub_msi", "MSI", "https://img.icons8.com/color/96/msi.png", "cat_laptop"));

        // 3. Tạo danh mục con cho Điện thoại
        saveCategory(db, new Category("sub_iphone", "iPhone", "https://img.icons8.com/color/96/apple-logo.png", "cat_phone"));
        saveCategory(db, new Category("sub_samsung", "Samsung", "https://img.icons8.com/color/96/samsung.png", "cat_phone"));
        saveCategory(db, new Category("sub_xiaomi", "Xiaomi", "https://img.icons8.com/color/96/xiaomi.png", "cat_phone"));
        saveCategory(db, new Category("sub_oppo", "OPPO", "https://img.icons8.com/color/96/oppo.png", "cat_phone"));
        saveCategory(db, new Category("sub_vivo", "Vivo", "https://img.icons8.com/color/96/vivo.png", "cat_phone"));
        saveCategory(db, new Category("sub_realme", "Realme", "https://img.icons8.com/color/96/realme.png", "cat_phone"));
        saveCategory(db, new Category("sub_pixel", "Google Pixel", "https://img.icons8.com/color/96/google-logo.png", "cat_phone"));

        // 4. Tạo danh mục con cho Phụ kiện
        saveCategory(db, new Category("sub_headphone", "Tai nghe", "https://img.icons8.com/color/96/headphones.png", "cat_accessory"));
        saveCategory(db, new Category("sub_charger", "Sạc dự phòng", "https://img.icons8.com/color/96/powerbank.png", "cat_accessory"));
        saveCategory(db, new Category("sub_mouse", "Chuột", "https://img.icons8.com/color/96/mouse.png", "cat_accessory"));
        saveCategory(db, new Category("sub_keyboard", "Bàn phím", "https://img.icons8.com/color/96/keyboard.png", "cat_accessory"));
        saveCategory(db, new Category("sub_speaker", "Loa Bluetooth", "https://img.icons8.com/color/96/bluetooth-speaker.png", "cat_accessory"));
        saveCategory(db, new Category("sub_cable", "Cáp sạc", "https://img.icons8.com/color/96/usb-cable.png", "cat_accessory"));
        saveCategory(db, new Category("sub_case", "Ốp lưng", "https://img.icons8.com/color/96/smartphone-case.png", "cat_accessory"));

        Log.d(TAG, "Default categories and sub-categories seed process started with images");
    }

    private static void saveCategory(FirebaseFirestore db, Category category) {
        db.collection("categories").document(category.getId()).set(category);
    }
}
