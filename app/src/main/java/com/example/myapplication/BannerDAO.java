package com.example.myapplication;

import android.content.Context;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class BannerDAO {
    private FirebaseFirestore firestore;
    private CollectionReference bannersRef;

    public BannerDAO() {
        firestore = FirebaseFirestore.getInstance();
        bannersRef = firestore.collection("banners");
    }

    public BannerDAO(Context context) {
        this();
    }

    public Task<QuerySnapshot> getAllBanners() {
        return bannersRef.get();
    }

    public Task<Void> addBanner(Banner banner) {
        if (banner.getId() == null || banner.getId().isEmpty()) {
            banner.setId(bannersRef.document().getId());
        }
        return bannersRef.document(banner.getId()).set(banner);
    }

    public Task<Void> updateBanner(Banner banner) {
        return bannersRef.document(banner.getId()).set(banner);
    }

    public Task<Void> deleteBanner(String id) {
        return bannersRef.document(id).delete();
    }
}
