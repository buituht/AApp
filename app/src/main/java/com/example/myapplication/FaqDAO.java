package com.example.myapplication;

import android.content.Context;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class FaqDAO {
    private FirebaseFirestore firestore;
    private CollectionReference faqsRef;

    public FaqDAO() {
        firestore = FirebaseFirestore.getInstance();
        faqsRef = firestore.collection("faqs");
    }

    public FaqDAO(Context context) {
        this();
    }

    public Task<QuerySnapshot> getAllFaqs() {
        return faqsRef.get();
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
