package com.example.myapplication;

import android.content.Context;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class FavoriteDAO {
    private FirebaseFirestore firestore;
    private CollectionReference favoritesRef;

    public FavoriteDAO() {
        firestore = FirebaseFirestore.getInstance();
        favoritesRef = firestore.collection("favorites");
    }

    public FavoriteDAO(Context context) {
        this();
    }

    public Task<Void> addFavorite(String email, String productId) {
        String id = email + "_" + productId;
        Map<String, Object> favorite = new HashMap<>();
        favorite.put("userEmail", email);
        favorite.put("productId", productId);
        return favoritesRef.document(id).set(favorite);
    }

    public Task<Void> removeFavorite(String email, String productId) {
        String id = email + "_" + productId;
        return favoritesRef.document(id).delete();
    }

    public Task<QuerySnapshot> getFavorites(String email) {
        return favoritesRef.whereEqualTo("userEmail", email).get();
    }

    public Task<DocumentSnapshot> isFavorite(String email, String productId) {
        String id = email + "_" + productId;
        return favoritesRef.document(id).get();
    }
}
