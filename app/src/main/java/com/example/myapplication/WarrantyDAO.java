package com.example.myapplication;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class WarrantyDAO {
    private FirebaseFirestore db;
    private CollectionReference warrantiesRef;
    private CollectionReference claimsRef;

    public WarrantyDAO() {
        db = FirebaseFirestore.getInstance();
        warrantiesRef = db.collection("warranties");
        claimsRef = db.collection("warranty_claims");
    }

    public Task<Void> createWarrantyCard(WarrantyCard card) {
        String id = warrantiesRef.document().getId();
        card.setWarrantyId(id);
        return warrantiesRef.document(id).set(card);
    }

    public Task<QuerySnapshot> getWarrantiesByUser(String userEmail) {
        return warrantiesRef.whereEqualTo("userEmail", userEmail).get();
    }

    public Task<QuerySnapshot> getWarrantyByOrderId(String orderId) {
        return warrantiesRef.whereEqualTo("orderId", orderId).get();
    }

    public Task<Void> submitClaim(WarrantyClaim claim) {
        String id = claimsRef.document().getId();
        claim.setClaimId(id);
        return claimsRef.document(id).set(claim);
    }
    
    public Task<QuerySnapshot> getClaimsByUser(String userEmail) {
        return claimsRef.whereEqualTo("userEmail", userEmail).get();
    }

    public Task<QuerySnapshot> getClaimsByStatus(String status) {
        return claimsRef.whereEqualTo("status", status)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get();
    }

    public Task<Void> updateClaimStatus(String claimId, String newStatus) {
        return claimsRef.document(claimId).update("status", newStatus);
    }

    public CollectionReference getClaimsCollection() {
        return claimsRef;
    }
}
