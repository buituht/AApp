package com.example.myapplication;

import java.io.Serializable;
import java.util.Objects;

public class Category implements Serializable {
    private String id;
    private String name;
    private String imageUrl;
    private String parentId; // Added for subcategories

    public Category() {
        // Required for Firebase
    }

    public Category(String id, String name, String imageUrl) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.parentId = null; // Default to top-level category
    }

    public Category(String id, String name, String imageUrl, String parentId) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.parentId = parentId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(id, category.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
