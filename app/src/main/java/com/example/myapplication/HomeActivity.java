package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private List<Product> productList;
    private List<Product> filteredList;
    private ProductAdapter productAdapter;
    private BottomNavigationView bottomNavigationView;
    
    private EditText etSearch;
    private TextView tvCartCount, tvCountdown;
    private NonScrollListView lvProducts;
    private FloatingActionButton fabAddProduct, fabAddCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupAdapter();
        setupClickListeners();
        setupBottomNavigation();
        setupSearch();
        setupFlashSaleCountdown();
        setupCategoryFilters();
        loadProducts();
        updateAdminUI();
    }

    private void initViews() {
        etSearch = findViewById(R.id.et_search_home);
        tvCartCount = findViewById(R.id.tv_cart_count_home);
        tvCountdown = findViewById(R.id.tv_countdown_home);
        lvProducts = findViewById(R.id.lv_recommended_home);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        
        fabAddProduct = findViewById(R.id.fab_add_product_home);
        fabAddCategory = findViewById(R.id.fab_add_category_home);
        
        findViewById(R.id.btn_cart_home).setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));
        findViewById(R.id.btn_notification_home).setOnClickListener(v -> Toast.makeText(this, "Bạn có thông báo mới!", Toast.LENGTH_SHORT).show());
    }

    private void updateAdminUI() {
        if (fabAddProduct != null) fabAddProduct.setVisibility(View.GONE);
        if (fabAddCategory != null) fabAddCategory.setVisibility(MainActivity.isLoggedIn && MainActivity.isAdmin ? View.VISIBLE : View.GONE);
    }

    private void setupFlashSaleCountdown() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            int seconds = 7200; 
            @Override
            public void run() {
                int h = seconds / 3600;
                int m = (seconds % 3600) / 60;
                int s = seconds % 60;
                if (tvCountdown != null) {
                    tvCountdown.setText(String.format("%02d : %02d : %02d", h, m, s));
                }
                if (seconds > 0) {
                    seconds--;
                    handler.postDelayed(this, 1000);
                }
            }
        });
    }

    private void setupCategoryFilters() {
        findViewById(R.id.cat_apple_home).setOnClickListener(v -> filterByCategory("Apple"));
        findViewById(R.id.cat_samsung_home).setOnClickListener(v -> filterByCategory("Samsung"));
        findViewById(R.id.cat_xiaomi_home).setOnClickListener(v -> filterByCategory("Xiaomi"));
        findViewById(R.id.cat_all_home).setOnClickListener(v -> filterByCategory(""));
    }

    private void filterByCategory(String brand) {
        filteredList.clear();
        if (brand.isEmpty()) {
            filteredList.addAll(productList);
        } else {
            for (Product p : productList) {
                if (p.getName().toLowerCase().contains(brand.toLowerCase()) || 
                   (p.getCategory() != null && p.getCategory().toLowerCase().contains(brand.toLowerCase()))) {
                    filteredList.add(p);
                }
            }
        }
        productAdapter.notifyDataSetChanged();
    }

    private void setupAdapter() {
        productList = new ArrayList<>();
        filteredList = new ArrayList<>();
        productAdapter = new ProductAdapter(this, filteredList, new ProductAdapter.OnProductActionListener() {
            @Override
            public void onEdit(Product product) {}
            @Override
            public void onDelete(Product product) {}
            @Override
            public void onBuy(Product product) {
                CartActivity.cartItemList.add(product);
                updateCartBadge();
                Toast.makeText(HomeActivity.this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onItemClick(Product product) {
                Intent intent = new Intent(HomeActivity.this, ProductDetailActivity.class);
                intent.putExtra("product_data", product);
                startActivity(intent);
            }
        });
        productAdapter.setShowAdminActions(false);
        lvProducts.setAdapter(productAdapter);
    }

    private void updateCartBadge() {
        if (tvCartCount != null) {
            tvCartCount.setText(String.valueOf(CartActivity.cartItemList.size()));
        }
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }
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
                if (p.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(p);
                }
            }
        }
        productAdapter.notifyDataSetChanged();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        android.view.Menu menu = bottomNavigationView.getMenu();
        if (menu.findItem(R.id.nav_register) != null) menu.findItem(R.id.nav_register).setVisible(!MainActivity.isLoggedIn);
        if (menu.findItem(R.id.nav_favorites) != null) menu.findItem(R.id.nav_favorites).setVisible(MainActivity.isLoggedIn);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) return true;
            if (itemId == R.id.nav_products) {
                startActivity(new Intent(this, ProductListActivity.class));
                return true;
            }
            if (itemId == R.id.nav_register) {
                startActivity(new Intent(this, RegisterActivity.class));
                return true;
            }
            if (itemId == R.id.nav_favorites) {
                startActivity(new Intent(this, FavoriteActivity.class));
                return true;
            }
            if (itemId == R.id.nav_profile) {
                if (MainActivity.isLoggedIn) {
                    startActivity(new Intent(this, ProfileActivity.class));
                } else {
                    startActivity(new Intent(this, LoginActivity.class));
                }
                return true;
            }
            return false;
        });
    }

    private void setupClickListeners() {
        if (fabAddProduct != null) {
            fabAddProduct.setOnClickListener(v -> startActivity(new Intent(this, AddProductActivity.class)));
        }
        if (fabAddCategory != null) {
            fabAddCategory.setOnClickListener(v -> startActivity(new Intent(this, AddCategoryActivity.class)));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateAdminUI();
        setupBottomNavigation();
        loadProducts();
        updateCartBadge();
    }

    private void loadProducts() {
        productList.clear();
        productList.addAll(dbHelper.getAllProducts());
        filterProducts(etSearch.getText().toString());
    }
}
