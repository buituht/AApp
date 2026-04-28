package com.example.myapplication;

import android.content.Context;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class ReviewDAO {
    private FirebaseFirestore db;
    private CollectionReference reviewsRef;

    public ReviewDAO() {
        db = FirebaseFirestore.getInstance();
        reviewsRef = db.collection("reviews");
    }

    public ReviewDAO(Context context) {
        this();
    }

    public Task<Void> addReview(Review review) {
        String id = reviewsRef.document().getId();
        review.setReviewId(id);
        return reviewsRef.document(id).set(review);
    }

    // Lấy toàn bộ đánh giá của sản phẩm (Sẽ sắp xếp ở phía Client)
    public Task<QuerySnapshot> getReviewsByProduct(String productId) {
        return reviewsRef.whereEqualTo("productId", productId).get();
    }

    // Kiểm tra xem người dùng đã đánh giá sản phẩm này chưa
    public Task<QuerySnapshot> checkIfUserReviewed(String productId, String userEmail) {
        return reviewsRef.whereEqualTo("productId", productId)
                .whereEqualTo("userEmail", userEmail)
                .get();
    }
}
