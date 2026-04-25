package com.example.myapplication;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class UserDAO {
    private FirebaseFirestore db;
    private CollectionReference usersRef;

    public UserDAO() {
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");
    }

    public UserDAO(android.content.Context context) {
        this();
    }

    public Task<Void> addUser(User user) {
        return usersRef.document(user.getEmail()).set(user);
    }

    public Task<DocumentSnapshot> getUserByEmail(String email) {
        return usersRef.document(email).get();
    }

    public Task<QuerySnapshot> getUserByUsername(String username) {
        return usersRef.whereEqualTo("username", username).limit(1).get();
    }

    public Task<Void> updateUser(User user) {
        return usersRef.document(user.getEmail()).set(user);
    }

    public Task<Void> updateUserWithOldEmail(User user, String oldEmail) {
        if (oldEmail.equals(user.getEmail())) {
            return updateUser(user);
        } else {
            // Nếu email thay đổi, xóa document cũ và tạo mới vì email là ID
            return usersRef.document(oldEmail).delete().continueWithTask(task -> {
                return usersRef.document(user.getEmail()).set(user);
            });
        }
    }

    public Task<Void> addFavorite(String email, String productId) {
        return usersRef.document(email).update("favorites", FieldValue.arrayUnion(productId));
    }

    public Task<Void> removeFavorite(String email, String productId) {
        return usersRef.document(email).update("favorites", FieldValue.arrayRemove(productId));
    }

    public Task<DocumentSnapshot> getFavorites(String email) {
        return usersRef.document(email).get();
    }
}
