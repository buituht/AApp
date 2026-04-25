package com.example.myapplication;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ProductListActivity extends AppCompatActivity {

    private static final int VOICE_SEARCH_REQUEST_CODE = 100;
    private static final int PERMISSION_RECORD_AUDIO_CODE = 200;
    
    private DatabaseHelper dbHelper;
    private List<Product> productList;
    private List<Product> filteredList;
    private ProductGridAdapter productAdapter;
    private BottomNavigationView bottomNavigationView;
    private EditText etSearch;
    private TextView tvCartBadge;
    
    private long minPrice = 0;
    private long maxPrice = Long.MAX_VALUE;
    private int minRating = 0;
    private int sortType = 0; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupData();
        setupAdapter();
        setupSearch();
        setupBottomNavigation();
        updateCartBadge();
    }

    private void initViews() {
        etSearch = findViewById(R.id.et_search_products);
        tvCartBadge = findViewById(R.id.tv_cart_badge);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        
        findViewById(R.id.btn_cart_products).setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));
        findViewById(R.id.btn_voice_search).setOnClickListener(v -> checkPermissionAndStartVoiceSearch());
        findViewById(R.id.btn_filter_products).setOnClickListener(v -> showFilterDialog());
    }

    private void updateCartBadge() {
        if (tvCartBadge != null) {
            int count = CartActivity.cartItemList.size();
            if (count > 0) {
                tvCartBadge.setText(String.valueOf(count));
                tvCartBadge.setVisibility(View.VISIBLE);
            } else {
                tvCartBadge.setVisibility(View.GONE);
            }
        }
    }

    private void checkPermissionAndStartVoiceSearch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_RECORD_AUDIO_CODE);
        } else {
            startVoiceSearch();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_RECORD_AUDIO_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceSearch();
            } else {
                Toast.makeText(this, "Bạn cần cấp quyền Micro để sử dụng tính năng này", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startVoiceSearch() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Nói tên sản phẩm muốn tìm...");
        try {
            startActivityForResult(intent, VOICE_SEARCH_REQUEST_CODE);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(this, "Thiết bị chưa cài đặt ứng dụng Google hoặc không hỗ trợ giọng nói", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VOICE_SEARCH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                etSearch.setText(result.get(0));
            }
        }
    }

    private void showFilterDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_filter_products, null);
        EditText etMin = view.findViewById(R.id.et_min_price);
        EditText etMax = view.findViewById(R.id.et_max_price);
        Spinner spRating = view.findViewById(R.id.sp_rating_filter);
        RadioGroup rgSort = view.findViewById(R.id.rg_sort_options);

        String[] ratings = {"Tất cả", "4 sao trở lên", "5 sao"};
        ArrayAdapter<String> ratingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ratings);
        ratingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRating.setAdapter(ratingAdapter);

        if (minPrice > 0) etMin.setText(String.valueOf(minPrice));
        if (maxPrice < Long.MAX_VALUE) etMax.setText(String.valueOf(maxPrice));
        spRating.setSelection(minRating == 5 ? 2 : (minRating == 4 ? 1 : 0));
        
        switch (sortType) {
            case 1: rgSort.check(R.id.rb_sort_best_seller); break;
            case 2: rgSort.check(R.id.rb_sort_price_asc); break;
            case 3: rgSort.check(R.id.rb_sort_price_desc); break;
            default: rgSort.check(R.id.rb_sort_default); break;
        }

        new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("Áp dụng", (dialog, which) -> {
                    String minStr = etMin.getText().toString();
                    String maxStr = etMax.getText().toString();
                    minPrice = minStr.isEmpty() ? 0 : Long.parseLong(minStr);
                    maxPrice = maxStr.isEmpty() ? Long.MAX_VALUE : Long.parseLong(maxStr);
                    int ratingPos = spRating.getSelectedItemPosition();
                    minRating = (ratingPos == 1) ? 4 : (ratingPos == 2 ? 5 : 0);
                    int checkedId = rgSort.getCheckedRadioButtonId();
                    if (checkedId == R.id.rb_sort_best_seller) sortType = 1;
                    else if (checkedId == R.id.rb_sort_price_asc) sortType = 2;
                    else if (checkedId == R.id.rb_sort_price_desc) sortType = 3;
                    else sortType = 0;
                    applyAllFilters();
                })
                .setNeutralButton("Xóa lọc", (dialog, which) -> {
                    minPrice = 0; maxPrice = Long.MAX_VALUE; minRating = 0; sortType = 0;
                    applyAllFilters();
                })
                .show();
    }

    private void applyAllFilters() {
        String query = etSearch.getText().toString().toLowerCase();
        filteredList.clear();
        for (Product p : productList) {
            boolean matchesSearch = p.getName().toLowerCase().contains(query);
            long price = (p.getDiscountPrice() > 0) ? p.getDiscountPrice() : p.getPrice();
            boolean matchesPrice = price >= minPrice && price <= maxPrice;
            boolean matchesRating = p.getRating() >= minRating;
            if (matchesSearch && matchesPrice && matchesRating) filteredList.add(p);
        }
        if (sortType == 1) Collections.sort(filteredList, (p1, p2) -> p2.getSoldQuantity() - p1.getSoldQuantity());
        else if (sortType == 2) Collections.sort(filteredList, (p1, p2) -> Long.compare((p1.getDiscountPrice() > 0 ? p1.getDiscountPrice() : p1.getPrice()), (p2.getDiscountPrice() > 0 ? p2.getDiscountPrice() : p2.getPrice())));
        else if (sortType == 3) Collections.sort(filteredList, (p1, p2) -> Long.compare((p2.getDiscountPrice() > 0 ? p2.getDiscountPrice() : p2.getPrice()), (p1.getDiscountPrice() > 0 ? p1.getDiscountPrice() : p1.getPrice())));
        productAdapter.notifyDataSetChanged();
    }

    private void setupData() {
        productList = new ArrayList<>();
        filteredList = new ArrayList<>();
    }

    private void setupAdapter() {
        GridView gvProducts = findViewById(R.id.gv_all_products);
        productAdapter = new ProductGridAdapter(this, filteredList, new ProductGridAdapter.OnProductActionListener() {
            @Override
            public void onBuy(Product product) {
                CartActivity.cartItemList.add(product);
                updateCartBadge(); // Cập nhật số lượng trên icon giỏ hàng ngay lập tức
                Toast.makeText(ProductListActivity.this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onItemClick(Product product) {
                Intent intent = new Intent(ProductListActivity.this, ProductDetailActivity.class);
                intent.putExtra("product_data", product);
                startActivity(intent);
            }
        });
        gvProducts.setAdapter(productAdapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { applyAllFilters(); }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_products);
        android.view.Menu menu = bottomNavigationView.getMenu();
        menu.findItem(R.id.nav_register).setVisible(!MainActivity.isLoggedIn);
        menu.findItem(R.id.nav_favorites).setVisible(MainActivity.isLoggedIn);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) { startActivity(new Intent(this, MainActivity.class)); return true; }
            else if (itemId == R.id.nav_products) return true;
            else if (itemId == R.id.nav_register) { startActivity(new Intent(this, RegisterActivity.class)); return true; }
            else if (itemId == R.id.nav_favorites) { startActivity(new Intent(this, FavoriteActivity.class)); return true; }
            else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, MainActivity.isLoggedIn ? ProfileActivity.class : LoginActivity.class));
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupBottomNavigation();
        loadProductsFromSQLite();
        updateCartBadge(); // Cập nhật lại số lượng khi quay về từ màn hình khác
    }

    private void loadProductsFromSQLite() {
        productList.clear();
        productList.addAll(dbHelper.getAllProducts());
        applyAllFilters();
    }
}
