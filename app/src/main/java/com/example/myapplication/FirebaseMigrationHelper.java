package com.example.myapplication;

import android.content.Context;
import android.util.Log;
import java.util.List;

public class FirebaseMigrationHelper {
    private static final String TAG = "MigrationHelper";
    private ProductDAO productDAO;
    private CategoryDAO categoryDAO;
    private BannerDAO bannerDAO;
    private FaqDAO faqDAO;

    public FirebaseMigrationHelper(Context context) {
        productDAO = new ProductDAO(context);
        categoryDAO = new CategoryDAO(context);
        bannerDAO = new BannerDAO(context);
        faqDAO = new FaqDAO(context);
    }

    public void migrateAll() {
        migrateCategories();
        migrateProducts();
        migrateBanners();
        migrateFaqs();
    }

    private void migrateCategories() {
        List<Category> categories = categoryDAO.getAllCategoriesSQLite();
        for (Category category : categories) {
            categoryDAO.addCategoryFirebase(category)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Migrated category: " + category.getName()))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to migrate category: " + category.getName(), e));
        }
    }

    private void migrateProducts() {
        List<Product> products = productDAO.getAllProductsFromSQLite();
        if (products != null) {
            for (Product product : products) {
                productDAO.updateProduct(product) // updateProduct in ProductDAO uses .set() which works for adding too
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Migrated product: " + product.getName()))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to migrate product: " + product.getName(), e));
            }
        }
    }

    private void migrateBanners() {
        List<Banner> banners = bannerDAO.getAllBannersSQLite();
        for (Banner banner : banners) {
            bannerDAO.addBannerFirebase(banner)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Migrated banner"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to migrate banner", e));
        }
    }

    private void migrateFaqs() {
        List<Faq> faqs = faqDAO.getAllFaqsSQLite();
        for (Faq faq : faqs) {
            faqDAO.addFaqFirebase(faq)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Migrated FAQ: " + faq.getQuestion()))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to migrate FAQ", e));
        }
    }
}
