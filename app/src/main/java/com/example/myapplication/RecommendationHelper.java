package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommendationHelper {
    private static final String PREF_NAME = "RecommendationPrefs";
    private static final String KEY_VIEWED_CATEGORIES = "viewed_categories";
    private static final String KEY_VIEWED_PRODUCTS = "viewed_products";
    private static final int MAX_HISTORY = 10;

    private SharedPreferences prefs;
    private Gson gson;

    public RecommendationHelper(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // Ghi lại việc người dùng xem một sản phẩm
    public void recordProductView(Product product) {
        if (product == null) return;

        // Lưu category count
        Map<String, Integer> categoryCounts = getCategoryCounts();
        String category = product.getCategory();
        if (category != null) {
            categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
            saveCategoryCounts(categoryCounts);
        }

        // Lưu danh sách sản phẩm đã xem gần đây
        List<String> viewedProductIds = getViewedProductIds();
        viewedProductIds.remove(product.getId()); // Xóa nếu đã tồn tại để đưa lên đầu
        viewedProductIds.add(0, product.getId());
        if (viewedProductIds.size() > MAX_HISTORY) {
            viewedProductIds.remove(viewedProductIds.size() - 1);
        }
        saveViewedProductIds(viewedProductIds);
    }

    public Map<String, Integer> getCategoryCounts() {
        String json = prefs.getString(KEY_VIEWED_CATEGORIES, "");
        if (json.isEmpty()) return new HashMap<>();
        Type type = new TypeToken<Map<String, Integer>>() {}.getType();
        return gson.fromJson(json, type);
    }

    private void saveCategoryCounts(Map<String, Integer> counts) {
        prefs.edit().putString(KEY_VIEWED_CATEGORIES, gson.toJson(counts)).apply();
    }

    public List<String> getViewedProductIds() {
        String json = prefs.getString(KEY_VIEWED_PRODUCTS, "");
        if (json.isEmpty()) return new ArrayList<>();
        Type type = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(json, type);
    }

    private void saveViewedProductIds(List<String> ids) {
        prefs.edit().putString(KEY_VIEWED_PRODUCTS, gson.toJson(ids)).apply();
    }

    // Lấy Category mà người dùng quan tâm nhất
    public String getMostInterestedCategory() {
        Map<String, Integer> counts = getCategoryCounts();
        String mostInterested = null;
        int max = 0;
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                mostInterested = entry.getKey();
            }
        }
        return mostInterested;
    }
}
