package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ProductDAO productDAO;
    private CategoryDAO categoryDAO;
    private BannerDAO bannerDAO;
    private FaqDAO faqDAO;
    private RecommendationHelper recommendationHelper;
    
    private List<Product> productList;
    private List<Product> filteredList;
    private ProductAdapter productAdapter;

    private List<Product> hotDiscountList;
    private ProductAdapter hotDiscountAdapter;
    private NonScrollListView lvHotDiscount;

    private List<Product> newArrivalList;
    private ProductAdapter newArrivalAdapter;
    private NonScrollListView lvNewArrival;

    private List<Product> recommendedList;
    private ProductAdapter recommendedAdapter;
    private NonScrollListView lvRecommendations;
    private TextView tvTitleRecommendations;
    
    private List<Category> categoryList;
    private List<Category> allCategoriesFromFirebase = new ArrayList<>();
    private HomeCategoryAdapter categoryAdapter;
    private RecyclerView rvCategories;

    private List<Faq> faqList;
    private FaqAdapter faqAdapter;
    private RecyclerView rvFaqs;

    private List<Banner> bannerList;
    private BannerAdapter bannerAdapter;
    private ViewPager2 vpBanners;
    private Handler bannerHandler = new Handler(Looper.getMainLooper());
    private Runnable bannerRunnable;

    public static boolean isLoggedIn = false;
    public static boolean isAdmin = false;
    public static User currentUser = null;

    private Button btnLoginNav;
    private ImageButton btnLogout;
    private FloatingActionButton fabAdd;
    private BottomNavigationView bottomNavigationView;
    
    private EditText etSearch;
    private TextView tvCartCount, tvCountdown;
    private NonScrollListView lvProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview);

        productDAO = new ProductDAO(this);
        categoryDAO = new CategoryDAO(this);
        bannerDAO = new BannerDAO(this);
        faqDAO = new FaqDAO(this);
        recommendationHelper = new RecommendationHelper(this);

        initViews();
        setupDataLists();
        setupAdapters();
        setupClickListeners();
        setupBottomNavigation();
        setupSearch();
        setupFlashSaleCountdown();
        
        loadProductsFromFirebase(); 
        loadCategoriesFromFirebase();
        loadBannersFromFirebase();
        loadFaqsFromFirebase();
        updateUIBasedOnLoginStatus();

        AdminCreator.createAdminAccount();
    }

    private void initViews() {
        etSearch = findViewById(R.id.et_search);
        tvCartCount = findViewById(R.id.tv_cart_count);
        tvCountdown = findViewById(R.id.tv_countdown);
        btnLoginNav = findViewById(R.id.btn_login_nav);
        btnLogout = findViewById(R.id.btn_logout);
        fabAdd = findViewById(R.id.fab_add);
        lvProducts = findViewById(R.id.lv_items);
        lvHotDiscount = findViewById(R.id.lv_hot_discount);
        lvNewArrival = findViewById(R.id.lv_new_arrival);
        
        lvRecommendations = findViewById(R.id.lv_recommendations);
        tvTitleRecommendations = findViewById(R.id.tv_title_recommendations);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        rvCategories = findViewById(R.id.rv_categories);
        rvFaqs = findViewById(R.id.rv_news);
        vpBanners = findViewById(R.id.vp_banners);
        
        findViewById(R.id.btn_cart).setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));
    }

    private void loadBannersFromFirebase() {
        bannerDAO.getAllBanners().addOnSuccessListener(queryDocumentSnapshots -> {
            bannerList.clear();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Banner banner = doc.toObject(Banner.class);
                if (banner != null) bannerList.add(banner);
            }
            bannerAdapter.notifyDataSetChanged();
            setupAutoSlide();
        }).addOnFailureListener(e -> Log.e(TAG, "Error loading banners", e));
    }

    private void setupAutoSlide() {
        if (bannerRunnable != null) bannerHandler.removeCallbacks(bannerRunnable);
        bannerRunnable = new Runnable() {
            @Override
            public void run() {
                if (bannerList.size() > 0) {
                    int nextItem = (vpBanners.getCurrentItem() + 1) % bannerList.size();
                    vpBanners.setCurrentItem(nextItem, true);
                    bannerHandler.postDelayed(this, 3000);
                }
            }
        };
        bannerHandler.postDelayed(bannerRunnable, 3000);
    }

    private void loadFaqsFromFirebase() {
        faqDAO.getAllFaqs().addOnSuccessListener(queryDocumentSnapshots -> {
            faqList.clear();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Faq faq = doc.toObject(Faq.class);
                if (faq != null) faqList.add(faq);
            }
            faqAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> Log.e(TAG, "Error loading FAQs", e));
    }

    private void setupFlashSaleCountdown() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            int seconds = 7200 + 900 + 45;
            @Override
            public void run() {
                int h = seconds / 3600;
                int m = (seconds % 3600) / 60;
                int s = seconds % 60;
                if (tvCountdown != null) tvCountdown.setText(String.format("%02d : %02d : %02d", h, m, s));
                if (seconds > 0) {
                    seconds--;
                    handler.postDelayed(this, 1000);
                }
            }
        });
    }

    private void loadCategoriesFromFirebase() {
        categoryDAO.getAllCategories().addOnSuccessListener(queryDocumentSnapshots -> {
            allCategoriesFromFirebase.clear();
            categoryList.clear();
            
            categoryList.add(new Category("all", "Tất cả", ""));
            
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Category cat = doc.toObject(Category.class);
                if (cat != null) {
                    cat.setId(doc.getId());
                    allCategoriesFromFirebase.add(cat);
                }
            }

            for (Category cat : allCategoriesFromFirebase) {
                if (cat.getParentId() == null || cat.getParentId().isEmpty()) {
                    categoryList.add(cat);
                }
            }
            
            if (categoryList.size() <= 1) {
                categoryList.add(new Category("cat_laptop", "Laptop", ""));
                categoryList.add(new Category("cat_phone", "Điện thoại", ""));
                categoryList.add(new Category("cat_accessory", "Phụ kiện", ""));
            }
            
            categoryAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error loading categories", e);
            categoryList.clear();
            categoryList.add(new Category("all", "Tất cả", ""));
            categoryList.add(new Category("cat_laptop", "Laptop", ""));
            categoryList.add(new Category("cat_phone", "Điện thoại", ""));
            categoryList.add(new Category("cat_accessory", "Phụ kiện", ""));
            categoryAdapter.notifyDataSetChanged();
        });
    }

    private void filterByCategory(Category selectedCat) {
        filteredList.clear();
        String categoryName = selectedCat.getName();
        
        if (categoryName.equalsIgnoreCase("Tất cả")) {
            filteredList.addAll(productList);
        } else {
            Set<String> targetCategoryNames = new HashSet<>();
            targetCategoryNames.add(categoryName.toLowerCase());
            
            for (Category cat : allCategoriesFromFirebase) {
                if (selectedCat.getId().equals(cat.getParentId())) {
                    targetCategoryNames.add(cat.getName().toLowerCase());
                }
            }

            for (Product p : productList) {
                if (p.getCategory() != null && targetCategoryNames.contains(p.getCategory().toLowerCase())) {
                    filteredList.add(p);
                }
            }
        }
        productAdapter.notifyDataSetChanged();
    }

    private void setupDataLists() {
        productList = new ArrayList<>();
        filteredList = new ArrayList<>();
        hotDiscountList = new ArrayList<>();
        newArrivalList = new ArrayList<>();
        recommendedList = new ArrayList<>();
        categoryList = new ArrayList<>();
        faqList = new ArrayList<>();
        bannerList = new ArrayList<>();
    }

    private void setupAdapters() {
        ProductAdapter.OnProductActionListener productActionListener = new ProductAdapter.OnProductActionListener() {
            @Override
            public void onEdit(Product product) {}
            @Override
            public void onDelete(Product product) {}
            @Override
            public void onBuy(Product product) {
                CartActivity.cartItemList.add(product);
                updateCartBadge();
                Toast.makeText(MainActivity.this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onItemClick(Product product) {
                Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
                intent.putExtra("product_data", product);
                startActivity(intent);
            }
        };

        productAdapter = new ProductAdapter(this, filteredList, productActionListener);
        productAdapter.setShowAdminActions(false);
        lvProducts.setAdapter(productAdapter);

        hotDiscountAdapter = new ProductAdapter(this, hotDiscountList, productActionListener);
        hotDiscountAdapter.setShowAdminActions(false);
        lvHotDiscount.setAdapter(hotDiscountAdapter);

        newArrivalAdapter = new ProductAdapter(this, newArrivalList, productActionListener);
        newArrivalAdapter.setShowAdminActions(false);
        lvNewArrival.setAdapter(newArrivalAdapter);

        recommendedAdapter = new ProductAdapter(this, recommendedList, productActionListener);
        recommendedAdapter.setShowAdminActions(false);
        lvRecommendations.setAdapter(recommendedAdapter);
        
        categoryAdapter = new HomeCategoryAdapter(categoryList, category -> filterByCategory(category));
        rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        faqAdapter = new FaqAdapter(this, faqList, new FaqAdapter.OnFaqClickListener() {
            @Override
            public void onFaqClick(Faq faq) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(faq.getQuestion())
                        .setMessage(faq.getAnswer())
                        .setPositiveButton("Đóng", null)
                        .show();
            }
            @Override
            public void onEditClick(Faq faq) {}
            @Override
            public void onDeleteClick(Faq faq) {}
        });
        rvFaqs.setLayoutManager(new LinearLayoutManager(this));
        rvFaqs.setAdapter(faqAdapter);

        bannerAdapter = new BannerAdapter(bannerList);
        vpBanners.setAdapter(bannerAdapter);
    }

    private void updateCartBadge() {
        if (tvCartCount != null) tvCartCount.setText(String.valueOf(CartActivity.cartItemList.size()));
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { filterProducts(s.toString()); }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterProducts(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(productList);
        } else {
            for (Product p : productList) {
                if (p.getName().toLowerCase().contains(query.toLowerCase())) filteredList.add(p);
            }
        }
        productAdapter.notifyDataSetChanged();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) return true;
            if (itemId == R.id.nav_products) { startActivity(new Intent(this, ProductListActivity.class)); return true; }
            if (itemId == R.id.nav_favorites) { startActivity(new Intent(this, FavoriteActivity.class)); return true; }
            if (itemId == R.id.nav_profile) {
                if (isLoggedIn) startActivity(new Intent(this, ProfileActivity.class));
                else startActivity(new Intent(this, LoginActivity.class));
                return true;
            }
            return false;
        });
    }

    private void setupClickListeners() {
        btnLoginNav.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
        btnLogout.setOnClickListener(v -> {
            isLoggedIn = false; isAdmin = false; currentUser = null;
            updateUIBasedOnLoginStatus();
            setupBottomNavigation();
        });
    }

    private void updateUIBasedOnLoginStatus() {
        if (isLoggedIn) { btnLoginNav.setVisibility(View.GONE); btnLogout.setVisibility(View.VISIBLE); }
        else { btnLoginNav.setVisibility(View.VISIBLE); btnLogout.setVisibility(View.GONE); }
        if (productAdapter != null) productAdapter.notifyDataSetChanged();
        if (hotDiscountAdapter != null) hotDiscountAdapter.notifyDataSetChanged();
        if (newArrivalAdapter != null) newArrivalAdapter.notifyDataSetChanged();
        if (recommendedAdapter != null) recommendedAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUIBasedOnLoginStatus();
        setupBottomNavigation();
        loadProductsFromFirebase();
        loadCategoriesFromFirebase();
        loadBannersFromFirebase();
        loadFaqsFromFirebase();
        updateCartBadge();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bannerRunnable != null) bannerHandler.removeCallbacks(bannerRunnable);
    }

    private void loadProductsFromFirebase() {
        productDAO.getAllProducts().addOnSuccessListener(queryDocumentSnapshots -> {
            productList.clear(); hotDiscountList.clear(); newArrivalList.clear();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Product p = doc.toObject(Product.class);
                if (p != null) {
                    p.setId(doc.getId()); productList.add(p);
                    if (p.isHotDiscount()) hotDiscountList.add(p);
                    if (p.isNewArrival()) newArrivalList.add(p);
                }
            }
            if (findViewById(R.id.tv_title_hot_discount) != null) findViewById(R.id.tv_title_hot_discount).setVisibility(hotDiscountList.isEmpty() ? View.GONE : View.VISIBLE);
            if (findViewById(R.id.tv_title_new_arrival) != null) findViewById(R.id.tv_title_new_arrival).setVisibility(newArrivalList.isEmpty() ? View.GONE : View.VISIBLE);
            hotDiscountAdapter.notifyDataSetChanged();
            newArrivalAdapter.notifyDataSetChanged();
            loadRecommendations();
            filterProducts(etSearch.getText().toString());
        }).addOnFailureListener(e -> Log.e(TAG, "Error loading products", e));
    }

    private void loadRecommendations() {
        List<String> viewedIds = recommendationHelper.getViewedProductIds();
        if (viewedIds.isEmpty()) {
            loadLegacyRecommendations();
            return;
        }
        // Gợi ý sử dụng thuật toán Jaccard nội bộ, không dùng Gemini AI
        loadJaccardRecommendations();
    }

    private void loadJaccardRecommendations() {
        tvTitleRecommendations.setText("Gợi ý riêng cho bạn");
        tvTitleRecommendations.setVisibility(View.VISIBLE);

        List<String> viewedIds = recommendationHelper.getViewedProductIds();
        if (viewedIds.isEmpty()) {
            loadLegacyRecommendations();
            return;
        }

        Set<String> userInterestTags = new HashSet<>();
        for (String id : viewedIds) {
            for (Product p : productList) {
                if (p.getId().equals(id)) {
                    userInterestTags.addAll(recommendationHelper.getProductTags(p));
                    break;
                }
            }
        }

        recommendedList.clear();
        recommendedList.addAll(recommendationHelper.recommendSimilarProducts(userInterestTags, productList, viewedIds, 6));
        updateRecommendationUI();
    }

    private void loadLegacyRecommendations() {
        tvTitleRecommendations.setText("Gợi ý riêng cho bạn");
        String favCategory = recommendationHelper.getMostInterestedCategory();
        recommendedList.clear();
        if (favCategory != null && !favCategory.isEmpty()) {
            for (Product p : productList) {
                if (favCategory.equalsIgnoreCase(p.getCategory())) recommendedList.add(p);
                if (recommendedList.size() >= 6) break;
            }
        }
        updateRecommendationUI();
    }

    private void updateRecommendationUI() {
        if (recommendedList.isEmpty()) {
            tvTitleRecommendations.setVisibility(View.GONE);
            lvRecommendations.setVisibility(View.GONE);
        } else {
            tvTitleRecommendations.setVisibility(View.VISIBLE);
            lvRecommendations.setVisibility(View.VISIBLE);
            recommendedAdapter.notifyDataSetChanged();
        }
    }
}
