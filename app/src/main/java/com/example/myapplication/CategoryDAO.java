package com.example.myapplication;

import android.content.Context;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.List;

public class CategoryDAO {
    private FirebaseFirestore firestore;
    private CollectionReference categoriesRef;
    private DatabaseHelper dbHelper;

    public CategoryDAO() {
        firestore = FirebaseFirestore.getInstance();
        categoriesRef = firestore.collection("categories");
    }

    public CategoryDAO(Context context) {
        this();
        this.dbHelper = new DatabaseHelper(context);
    }

    public List<Category> getAllCategoriesSQLite() {
        return dbHelper != null ? dbHelper.getAllCategories() : null;
    }

    public Task<Void> addCategoryFirebase(Category category) {
        return addCategory(category);
    }

    public Task<QuerySnapshot> getAllCategoriesFirebase() {
        return categoriesRef.get();
    }

    public Task<QuerySnapshot> getAllCategories() {
        return getAllCategoriesFirebase();
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
