package com.example.myapplication;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class OrderDAO {
    private FirebaseFirestore db;
    private CollectionReference ordersRef;

    public OrderDAO() {
        db = FirebaseFirestore.getInstance();
        ordersRef = db.collection("orders");
    }

    public Task<Void> addOrder(Order order) {
        if (order.getOrderId() == null || order.getOrderId().isEmpty()) {
            order.setOrderId(ordersRef.document().getId());
        }
        return ordersRef.document(order.getOrderId()).set(order);
    }

    public Task<Void> updateOrderStatus(String orderId, String status) {
        return ordersRef.document(orderId).update("status", status);
    }

    public Task<DocumentSnapshot> getOrderById(String orderId) {
        return ordersRef.document(orderId).get();
    }

    // Lọc theo Email người dùng để chính xác tuyệt đối
    public Task<QuerySnapshot> getOrdersByUserEmail(String email) {
        return ordersRef.whereEqualTo("userEmail", email)
                .get();
    }

    public Task<QuerySnapshot> getOrdersByUsername(String username) {
        return ordersRef.whereEqualTo("username", username)
                .get();
    }

    public Task<QuerySnapshot> getAllOrders() {
        return ordersRef.orderBy("timestamp", Query.Direction.DESCENDING).get();
    }
    
    public CollectionReference getCollectionReference() {
        return ordersRef;
    }
}
