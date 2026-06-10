package com.example.myapplication;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Product implements Serializable {
    private String name;
    private long price;
    private long discountPrice;
    private String description;
    private Object images;
    private String category;
    private int rating;
    private int soldQuantity;
    private boolean hotDiscount;
    private boolean newArrival;
    private boolean bestSeller;
    private String screen;
    private String cpu;
    private String ram;
    private String rom;
    private String camera;
    private String battery;
    private String warranty; 
    
    @Exclude
    private String documentId; // Dùng để lưu ID từ Firestore thủ công
    
    private String firebaseId; // Trường khớp với dữ liệu trên Firestore

    public Product() {
    }

    @Exclude
    public String getDocumentId() { return documentId; }
    @Exclude
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getFirebaseId() { return firebaseId; }
    public void setFirebaseId(String firebaseId) { this.firebaseId = firebaseId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getPrice() { return price; }
    public void setPrice(long price) { this.price = price; }

    public long getDiscountPrice() { return discountPrice; }
    public void setDiscountPrice(long discountPrice) { this.discountPrice = discountPrice; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getImages() {
        List<String> result = new ArrayList<>();
        if (images instanceof List<?>) {
            List<?> list = (List<?>) images;
            for (Object obj : list) {
                if (obj instanceof String) {
                    result.add((String) obj);
                }
            }
        } else if (images instanceof String) {
            result.add((String) images);
        }
        return result;
    }
    public void setImages(Object images) { this.images = images; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public int getSoldQuantity() { return soldQuantity; }
    public void setSoldQuantity(int soldQuantity) { this.soldQuantity = soldQuantity; }

    @PropertyName("hotDiscount")
    public boolean isHotDiscount() { return hotDiscount; }
    @PropertyName("hotDiscount")
    public void setHotDiscount(boolean hotDiscount) { this.hotDiscount = hotDiscount; }

    @PropertyName("newArrival")
    public boolean isNewArrival() { return newArrival; }
    @PropertyName("newArrival")
    public void setNewArrival(boolean newArrival) { this.newArrival = newArrival; }

    @PropertyName("bestSeller")
    public boolean isBestSeller() { return bestSeller; }
    @PropertyName("bestSeller")
    public void setBestSeller(boolean bestSeller) { this.bestSeller = bestSeller; }

    public String getScreen() { return screen; }
    public void setScreen(String screen) { this.screen = screen; }

    public String getCpu() { return cpu; }
    public void setCpu(String cpu) { this.cpu = cpu; }

    public String getRam() { return ram; }
    public void setRam(String ram) { this.ram = ram; }

    public String getRom() { return rom; }
    public void setRom(String rom) { this.rom = rom; }

    public String getCamera() { return camera; }
    public void setCamera(String camera) { this.camera = camera; }

    public String getBattery() { return battery; }
    public void setBattery(String battery) { this.battery = battery; }

    public String getWarranty() { return warranty; }
    public void setWarranty(String warranty) { this.warranty = warranty; }

    public String getId() { 
        if (firebaseId != null && !firebaseId.isEmpty()) return firebaseId;
        if (documentId != null && !documentId.isEmpty()) return documentId;
        return null;
    }

    public void setId(String id) {
        this.documentId = id;
    }
}
