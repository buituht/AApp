package com.example.myapplication;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.HashMap;
import java.util.Map;

public class OrderDAO {
    private FirebaseFirestore db;
    private CollectionReference ordersRef;

    public OrderDAO() {
        db = FirebaseFirestore.getInstance();
        ordersRef = db.collection("orders");
    }

    public Task<Void> placeOrder(Order order) {
        String id = ordersRef.document().getId();
        order.setOrderId(id);
        return ordersRef.document(id).set(order);
    }

    public Task<DocumentSnapshot> getOrderById(String orderId) {
        return ordersRef.document(orderId).get();
    }

    public Task<QuerySnapshot> getOrdersByUsername(String username) {
        return ordersRef.whereEqualTo("username", username)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get();
    }

    public Task<QuerySnapshot> getAllOrders() {
        return ordersRef.orderBy("timestamp", Query.Direction.DESCENDING).get();
    }

    public Task<Void> updateOrderStatus(String orderId, String newStatus) {
        return ordersRef.document(orderId).update("status", newStatus);
    }

    public Task<Void> cancelOrderWithReason(String orderId, String reason) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "Đã hủy");
        updates.put("cancelReason", reason);
        return ordersRef.document(orderId).update(updates);
    }

    public CollectionReference getOrdersRef() {
        return ordersRef;
    }
}
