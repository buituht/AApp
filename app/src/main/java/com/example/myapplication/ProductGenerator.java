package com.example.myapplication;

import android.util.Log;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ProductGenerator {
    public static void generate50Products() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // 1. Xóa toàn bộ sản phẩm cũ trước khi tạo mới
        db.collection("products").get().addOnSuccessListener(queryDocumentSnapshots -> {
            WriteBatch deleteBatch = db.batch();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                deleteBatch.delete(doc.getReference());
            }
            
            deleteBatch.commit().addOnSuccessListener(aVoid -> {
                Log.d("ProductGenerator", "Deleted old products. Generating new 50 products...");
                createNewProducts(db);
            });
        }).addOnFailureListener(e -> {
            Log.e("ProductGenerator", "Error deleting products", e);
            createNewProducts(db); // Vẫn thử tạo mới nếu xóa lỗi
        });
    }

    private static void createNewProducts(FirebaseFirestore db) {
        WriteBatch batch = db.batch();
        Random random = new Random();
        
        // Cửa hàng chuyên thiết bị điện tử
        String[] categories = {"Điện thoại", "Laptop", "Phụ kiện", "Tablet"};
        
        String[] phoneModels = {"iPhone 15 Pro", "Samsung Galaxy S24 Ultra", "Xiaomi 14", "Oppo Find X7", "Google Pixel 8", "Sony Xperia 1 V", "Realme GT5", "Vivo X100"};
        String[] laptopModels = {"MacBook Air M3", "Dell XPS 15", "HP Spectre x360", "ASUS ROG Zephyrus", "Lenovo ThinkPad X1 Carbon", "Acer Predator Helios", "MSI Katana"};
        String[] accessoryModels = {"AirPods Pro 2", "Sony WH-1000XM5", "Logitech MX Master 3S", "Bàn phím cơ AKKO", "Sạc dự phòng Anker 20000mAh", "Apple Watch Series 9", "Samsung Galaxy Watch 6"};
        String[] tabletModels = {"iPad Pro M2", "Samsung Galaxy Tab S9", "Xiaomi Pad 6", "Microsoft Surface Pro 9"};

        for (int i = 1; i <= 50; i++) {
            Product p = new Product();
            String category = categories[random.nextInt(categories.length)];
            String name = "";
            String searchKeyword = "";
            
            // Cấu hình mẫu dựa trên category
            switch (category) {
                case "Điện thoại":
                    name = phoneModels[random.nextInt(phoneModels.length)] + " (" + (i + 100) + ")";
                    searchKeyword = "iphone,smartphone";
                    p.setScreen("6.1 - 6.8 inch, OLED");
                    p.setCpu("A17 Bionic / Snapdragon 8 Gen 3");
                    p.setRam("8GB - 12GB");
                    p.setRom("128GB - 512GB");
                    p.setBattery("4500mAh - 5000mAh");
                    break;
                case "Laptop":
                    name = laptopModels[random.nextInt(laptopModels.length)] + " " + (i + 2024);
                    searchKeyword = "laptop,macbook";
                    p.setScreen("13 - 16 inch, 4K");
                    p.setCpu("Intel Core i7 / Apple M3");
                    p.setRam("16GB - 32GB");
                    p.setRom("512GB - 1TB");
                    p.setBattery("70Wh - 99Wh");
                    break;
                case "Phụ kiện":
                    name = accessoryModels[random.nextInt(accessoryModels.length)] + " v" + i;
                    searchKeyword = "headphone,keyboard,smartwatch";
                    p.setScreen("N/A");
                    p.setCpu("Chip xử lý thông minh");
                    p.setRam("N/A");
                    p.setRom("N/A");
                    p.setBattery("Tùy loại phụ kiện");
                    break;
                case "Tablet":
                    name = tabletModels[random.nextInt(tabletModels.length)] + " Gen " + (i % 5 + 1);
                    searchKeyword = "ipad,tablet";
                    p.setScreen("11 - 12.9 inch, Liquid Retina");
                    p.setCpu("Apple M2 / Snapdragon 8 Gen 2");
                    p.setRam("8GB - 16GB");
                    p.setRom("128GB - 2TB");
                    p.setBattery("8000mAh - 10000mAh");
                    break;
            }

            p.setName(name);
            p.setCategory(category);
            p.setDescription("Sản phẩm " + name + " cao cấp, chính hãng, bảo hành 12 tháng. Phù hợp cho công việc, giải trí và học tập với công nghệ mới nhất hiện nay.");
            
            long price = 500000 + random.nextInt(40000000);
            p.setPrice(price);
            p.setDiscountPrice(price - (random.nextInt(10) > 6 ? random.nextInt(2000000) : 0));
            
            p.setRating(4 + random.nextInt(2));
            p.setSoldQuantity(random.nextInt(1000));
            p.setHotDiscount(random.nextInt(10) > 7);
            p.setNewArrival(random.nextInt(10) > 5);
            p.setBestSeller(random.nextInt(10) > 8);

            // 10 ảnh thiết bị điện tử thực tế
            List<String> imageList = new ArrayList<>();
            for (int j = 0; j < 10; j++) {
                imageList.add("https://loremflickr.com/500/500/" + searchKeyword + "?lock=" + (i * 10 + j));
            }
            p.setImages(imageList);

            String docId = db.collection("products").document().getId();
            p.setFirebaseId(docId);
            p.setId(docId);
            
            batch.set(db.collection("products").document(docId), p);
        }

        batch.commit().addOnSuccessListener(aVoid -> {
            Log.d("ProductGenerator", "Successfully created 50 new electronic products");
        }).addOnFailureListener(e -> {
            Log.e("ProductGenerator", "Error committing batch", e);
        });
    }
}
