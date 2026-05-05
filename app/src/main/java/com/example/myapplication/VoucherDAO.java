package com.example.myapplication;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

public class VoucherDAO {
    private FirebaseFirestore firestore;
    private CollectionReference vouchersRef;

    public VoucherDAO() {
        firestore = FirebaseFirestore.getInstance();
        vouchersRef = firestore.collection("vouchers");
    }

    public Task<QuerySnapshot> getAllVouchers() {
        return vouchersRef.get();
    }

    public Task<QuerySnapshot> getVoucherByCode(String code) {
        return vouchersRef.whereEqualTo("code", code).limit(1).get();
    }

    public Task<Void> addVoucher(Voucher voucher) {
        if (voucher.getId() == null || voucher.getId().isEmpty()) {
            String newId = vouchersRef.document().getId();
            voucher.setId(newId);
        }
        return vouchersRef.document(voucher.getId()).set(voucher);
    }

    public Task<Void> updateVoucher(Voucher voucher) {
        return vouchersRef.document(voucher.getId()).set(voucher);
    }

    public Task<Void> deleteVoucher(String id) {
        return vouchersRef.document(id).delete();
    }
}
