package com.example.myapplication;

import android.content.Context;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.List;

public class FaqDAO {
    private FirebaseFirestore firestore;
    private CollectionReference faqsRef;
    private DatabaseHelper dbHelper;

    public FaqDAO() {
        firestore = FirebaseFirestore.getInstance();
        faqsRef = firestore.collection("faqs");
    }

    public FaqDAO(Context context) {
        this();
        this.dbHelper = new DatabaseHelper(context);
    }

    public List<Faq> getAllFaqsSQLite() {
        return dbHelper != null ? dbHelper.getAllFaqs() : null;
    }

    public Task<Void> addFaqFirebase(Faq faq) {
        return addFaq(faq);
    }

    public Task<QuerySnapshot> getAllFaqsFirebase() {
        return faqsRef.get();
    }

    public Task<QuerySnapshot> getAllFaqs() {
        return getAllFaqsFirebase();
    }

    public Task<Void> addFaq(Faq faq) {
        if (faq.getId() == null || faq.getId().isEmpty()) {
            faq.setId(faqsRef.document().getId());
        }
        return faqsRef.document(faq.getId()).set(faq);
    }

    public Task<Void> updateFaq(Faq faq) {
        return faqsRef.document(faq.getId()).set(faq);
    }

    public Task<Void> deleteFaq(String id) {
        return faqsRef.document(id).delete();
    }
}
