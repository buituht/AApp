package com.example.myapplication;

import android.content.Context;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class CategoryDAO {
    private FirebaseFirestore firestore;
    private CollectionReference categoriesRef;

    public CategoryDAO() {
        firestore = FirebaseFirestore.getInstance();
        categoriesRef = firestore.collection("categories");
    }

    public CategoryDAO(Context context) {
        this();
    }

    public Task<QuerySnapshot> getAllCategories() {
        return categoriesRef.get();
    }

    public Task<Void> addCategory(Category category) {
        if (category.getId() == null || category.getId().isEmpty()) {
            category.setId(categoriesRef.document().getId());
        }
        return categoriesRef.document(category.getId()).set(category);
    }

    public Task<Void> updateCategory(Category category) {
        return categoriesRef.document(category.getId()).set(category);
    }

    public Task<Void> deleteCategory(String id) {
        return categoriesRef.document(id).delete();
    }
}
