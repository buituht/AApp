package com.example.myapplication;

import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Product implements Serializable {
    private String firebaseId; // Để khớp với trường firebaseId trong ảnh
    private String name;
    private long price;
    private long discountPrice;
    private String description;
    private Object images; // Dùng Object để chấp nhận cả String đơn lẻ hoặc List từ Firebase
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
    private Object id; // Chấp nhận cả số (long) hoặc chuỗi (String) từ Firebase

    public Product() {
    }

    @PropertyName("firebaseId")
    public String getFirebaseId() { return firebaseId; }
    @PropertyName("firebaseId")
    public void setFirebaseId(String firebaseId) { this.firebaseId = firebaseId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getPrice() { return price; }
    public void setPrice(long price) { this.price = price; }

    public long getDiscountPrice() { return discountPrice; }
    public void setDiscountPrice(long discountPrice) { this.discountPrice = discountPrice; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // Xử lý linh hoạt: Nếu images là String thì trả về List có 1 phần tử, nếu là List thì trả về chính nó
    public List<String> getImages() {
        if (images instanceof List) {
            return (List<String>) images;
        } else if (images instanceof String) {
            List<String> list = new ArrayList<>();
            list.add((String) images);
            return list;
        }
        return new ArrayList<>();
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

    public String getId() { return String.valueOf(id); }
    public void setId(Object id) { this.id = id; }
}
