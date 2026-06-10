package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    public void recordProductView(Product product) {
        if (product == null) return;

        Map<String, Integer> categoryCounts = getCategoryCounts();
        String category = product.getCategory();
        if (category != null) {
            categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
            saveCategoryCounts(categoryCounts);
        }

        List<String> viewedProductIds = getViewedProductIds();
        viewedProductIds.remove(product.getId());
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

    // --- Thuật toán Jaccard Similarity triển khai từ code của bạn ---

    public double similarity(Set<String> a, Set<String> b) {
        if (a.isEmpty() && b.isEmpty()) return 1.0;
        Set<String> intersection = new HashSet<>(a);
        intersection.retainAll(b);

        Set<String> union = new HashSet<>(a);
        union.addAll(b);

        if (union.isEmpty()) return 0.0;
        return (double) intersection.size() / union.size();
    }

    public Set<String> getProductTags(Product p) {
        Set<String> tags = new HashSet<>();
        if (p.getName() != null) {
            String[] words = p.getName().toLowerCase().split("\\s+");
            for (String w : words) if (w.length() > 2) tags.add(w);
        }
        if (p.getCategory() != null) {
            tags.add(p.getCategory().toLowerCase());
        }
        return tags;
    }

    public List<Product> recommendSimilarProducts(
            Set<String> userInterestTags,
            List<Product> allProducts,
            List<String> excludeIds,
            int limit
    ) {
        return allProducts.stream()
                .filter(p -> !excludeIds.contains(p.getId()))
                .sorted((p1, p2) -> Double.compare(
                        similarity(userInterestTags, getProductTags(p2)),
                        similarity(userInterestTags, getProductTags(p1))
                ))
                .limit(limit)
                .collect(Collectors.toList());
    }
}
