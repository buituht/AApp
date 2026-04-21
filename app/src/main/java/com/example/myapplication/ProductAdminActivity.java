package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class ProductAdminActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private List<Product> productList;
    private List<Product> filteredList;
    private ProductAdapter productAdapter;
    private EditText etSearch;
    private ListView lvProducts;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        // Kiểm tra quyền Admin
        if (!MainActivity.isLoggedIn || !MainActivity.isAdmin) {
            Toast.makeText(this, "Bạn không có quyền truy cập!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupData();
        setupAdapter();
        setupSearch();
        setupBottomNavigation();
        loadProductsFromSQLite();
    }

    private void initViews() {
        etSearch = findViewById(R.id.et_search_products);
        
        // Admin sử dụng ListView để dễ quản lý (sửa/xóa)
        View gv = findViewById(R.id.gv_all_products);
        if (gv != null) gv.setVisibility(View.GONE);

        ConstraintLayout root = findViewById(R.id.root_product_list);
        lvProducts = new ListView(this);
        lvProducts.setId(View.generateViewId());
        
        ConstraintLayout.LayoutParams lvParams = new ConstraintLayout.LayoutParams(
                0, 0
        );
        lvParams.topToBottom = R.id.tv_list_title;
        lvParams.bottomToTop = R.id.bottom_navigation;
        lvParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        lvParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        lvProducts.setLayoutParams(lvParams);
        root.addView(lvProducts);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        
        // Đổi tiêu đề header
        View header = findViewById(R.id.product_header);
        if (header != null) {
            setTitle("Quản lý sản phẩm");
        }

        // Hiện Bottom Navigation
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.VISIBLE);
        }

        // Thêm FAB
        fabAdd = new FloatingActionButton(this);
        fabAdd.setImageResource(android.R.drawable.ic_input_add);
        fabAdd.setOnClickListener(v -> startActivity(new Intent(this, AddProductActivity.class)));
        
        if (root != null) {
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
            );
            params.bottomToTop = R.id.bottom_navigation;
            params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
            params.setMargins(0, 0, 32, 32);
            fabAdd.setLayoutParams(params);
            root.addView(fabAdd);
        }
    }

    private void setupData() {
        productList = new ArrayList<>();
        filteredList = new ArrayList<>();
    }

    private void setupAdapter() {
        productAdapter = new ProductAdapter(this, filteredList, new ProductAdapter.OnProductActionListener() {
            @Override
            public void onEdit(Product product) {
                Intent intent = new Intent(ProductAdminActivity.this, AddProductActivity.class);
                intent.putExtra("is_edit", true);
                intent.putExtra("product_data", product);
                startActivity(intent);
            }

            @Override
            public void onDelete(Product product) {
                new AlertDialog.Builder(ProductAdminActivity.this)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc chắn muốn xóa sản phẩm: " + product.getName() + "?")
                        .setPositiveButton("Xóa", (dialog, which) -> deleteProduct(product))
                        .setNegativeButton("Hủy", null)
                        .show();
            }

            @Override
            public void onBuy(Product product) {
            }

            @Override
            public void onItemClick(Product product) {
                Intent intent = new Intent(ProductAdminActivity.this, AddProductActivity.class);
                intent.putExtra("is_edit", true);
                intent.putExtra("product_data", product);
                startActivity(intent);
            }
        });
        productAdapter.setShowAdminActions(true);
        lvProducts.setAdapter(productAdapter);
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
        if (bottomNavigationView == null) return;
        
        bottomNavigationView.setSelectedItemId(R.id.nav_products);
        
        android.view.Menu menu = bottomNavigationView.getMenu();
        if (menu.findItem(R.id.nav_register) != null) {
            menu.findItem(R.id.nav_register).setVisible(!MainActivity.isLoggedIn);
        }
        if (menu.findItem(R.id.nav_favorites) != null) {
            menu.findItem(R.id.nav_favorites).setVisible(MainActivity.isLoggedIn);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (itemId == R.id.nav_products) {
                return true;
            } else if (itemId == R.id.nav_register) {
                startActivity(new Intent(this, RegisterActivity.class));
                return true;
            } else if (itemId == R.id.nav_favorites) {
                startActivity(new Intent(this, FavoriteActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
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

    private void loadProductsFromSQLite() {
        productList.clear();
        productList.addAll(dbHelper.getAllProducts());
        filterProducts(etSearch.getText().toString());
    }

    private void deleteProduct(Product product) {
        dbHelper.deleteProduct(product.getId());
        Toast.makeText(this, "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
        loadProductsFromSQLite();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_products);
        }
        loadProductsFromSQLite();
    }
}
