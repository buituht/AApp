package com.example.myapplication;

import android.content.Context;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.List;

public class BannerDAO {
    private FirebaseFirestore firestore;
    private CollectionReference bannersRef;
    private DatabaseHelper dbHelper;

    public BannerDAO() {
        firestore = FirebaseFirestore.getInstance();
        bannersRef = firestore.collection("banners");
    }

    public BannerDAO(Context context) {
        this();
        this.dbHelper = new DatabaseHelper(context);
    }

    public List<Banner> getAllBannersSQLite() {
        return dbHelper != null ? dbHelper.getAllBanners() : null;
    }

    public Task<Void> addBannerFirebase(Banner banner) {
        return addBanner(banner);
    }

    public Task<QuerySnapshot> getAllBannersFirebase() {
        return bannersRef.get();
    }

    public Task<QuerySnapshot> getAllBanners() {
        return getAllBannersFirebase();
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
