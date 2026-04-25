package com.example.myapplication;

import android.content.Context;
import android.net.Uri;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    private FirebaseFirestore db;
    private CollectionReference productsRef;
    private FirebaseStorage storage;
    private DatabaseHelper dbHelper;

    public ProductDAO() {
        db = FirebaseFirestore.getInstance();
        productsRef = db.collection("products");
        storage = FirebaseStorage.getInstance();
    }

    public ProductDAO(Context context) {
        this();
        this.dbHelper = new DatabaseHelper(context);
    }

    public List<Product> getAllProductsFromSQLite() {
        return dbHelper != null ? dbHelper.getAllProducts() : null;
    }

    // Lấy toàn bộ sản phẩm từ Firebase
    public Task<QuerySnapshot> getAllProducts() {
        return productsRef.get();
    }

    // Thêm sản phẩm mới kèm upload ảnh
    public Task<Void> addProduct(Product product) {
        DocumentReference docRef = productsRef.document();
        product.setFirebaseId(docRef.getId());
        // Nếu bạn muốn đồng nhất id, có thể dùng: product.setId(docRef.getId());
        
        return uploadImages(product).onSuccessTask(urls -> {
            product.setImages(urls);
            return docRef.set(product);
        });
    }

    // Cập nhật sản phẩm
    public Task<Void> updateProduct(Product product) {
        String id = (product.getFirebaseId() != null && !product.getFirebaseId().isEmpty()) 
                    ? product.getFirebaseId() 
                    : (product.getId() != null ? product.getId() : productsRef.document().getId());
        
        product.setFirebaseId(id);
        
        return uploadImages(product).onSuccessTask(urls -> {
            product.setImages(urls);
            return productsRef.document(id).set(product);
        });
    }

    public Task<Void> deleteProduct(String firebaseId) {
        return productsRef.document(firebaseId).delete();
    }

    // Logic hỗ trợ upload nhiều ảnh cùng lúc
    private Task<List<String>> uploadImages(Product product) {
        List<String> images = product.getImages();
        if (images == null || images.isEmpty()) {
            return Tasks.forResult(new ArrayList<>());
        }
        
        List<Task<Uri>> uploadTasks = new ArrayList<>();

        for (String path : images) {
            if (path == null) continue;
            if (path.startsWith("http")) {
                // Ảnh đã được upload rồi, giữ nguyên URL
                uploadTasks.add(Tasks.forResult(Uri.parse(path)));
            } else {
                // Upload ảnh từ máy lên Storage
                File file = new File(path);
                if (file.exists()) {
                    StorageReference ref = storage.getReference().child("products/" + System.currentTimeMillis() + "_" + file.getName());
                    Task<Uri> uploadTask = ref.putFile(Uri.fromFile(file))
                            .continueWithTask(task -> ref.getDownloadUrl());
                    uploadTasks.add(uploadTask);
                } else {
                    // If file doesn't exist, we can't upload it. Just skip or return empty.
                    // For now, let's just return the path as is if it's not a local file we can find
                    uploadTasks.add(Tasks.forResult(Uri.parse(path)));
                }
            }
        }

        if (uploadTasks.isEmpty()) {
            return Tasks.forResult(new ArrayList<>());
        }

        return Tasks.whenAllSuccess(uploadTasks);
    }

    public CollectionReference getCollectionReference() {
        return productsRef;
    }
}
