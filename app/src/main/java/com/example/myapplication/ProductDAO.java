package com.example.myapplication;

import android.content.Context;
import android.net.Uri;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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

    public ProductDAO() {
        db = FirebaseFirestore.getInstance();
        productsRef = db.collection("products");
        storage = FirebaseStorage.getInstance();
    }

    public ProductDAO(Context context) {
        this();
    }

    public Task<QuerySnapshot> getAllProducts() {
        return productsRef.get();
    }

    public Task<DocumentSnapshot> getProductById(String id) {
        return productsRef.document(id).get();
    }

    public Task<Void> addProduct(Product product) {
        DocumentReference docRef = productsRef.document();
        product.setFirebaseId(docRef.getId());

        return uploadImages(product).onSuccessTask(urls -> {
            product.setImages(urls);
            return docRef.set(product);
        });
    }

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

    private Task<List<String>> uploadImages(Product product) {
        List<String> images = product.getImages();
        if (images == null || images.isEmpty()) {
            return Tasks.forResult(new ArrayList<>());
        }

        List<Task<Uri>> uploadTasks = new ArrayList<>();

        for (String path : images) {
            if (path == null) continue;
            if (path.startsWith("http")) {
                uploadTasks.add(Tasks.forResult(Uri.parse(path)));
            } else {
                File file = new File(path);
                if (file.exists()) {
                    StorageReference ref = storage.getReference().child("products/" + System.currentTimeMillis() + "_" + file.getName());
                    Task<Uri> uploadTask = ref.putFile(Uri.fromFile(file))
                            .continueWithTask(task -> ref.getDownloadUrl());
                    uploadTasks.add(uploadTask);
                } else {
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
