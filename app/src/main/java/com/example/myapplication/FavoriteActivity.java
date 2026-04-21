package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;

public class FavoriteActivity extends AppCompatActivity {

    private GridView gvFavorites;
    private ProductAdapter adapter;
    private List<Product> favoriteList;
    private TextView tvEmpty;
    private BottomNavigationView bottomNavigationView;
    private FavoriteDAO favoriteDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        if (!MainActivity.isLoggedIn || MainActivity.currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem yêu thích!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        favoriteDAO = new FavoriteDAO(this);
        
        gvFavorites = findViewById(R.id.gv_favorites);
        tvEmpty = findViewById(R.id.tv_empty_favorites);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        favoriteList = new ArrayList<>();
        adapter = new ProductAdapter(this, favoriteList, new ProductAdapter.OnProductActionListener() {
            @Override
            public void onEdit(Product product) {}
            @Override
            public void onDelete(Product product) {}
            @Override
            public void onBuy(Product product) {
                CartActivity.cartItemList.add(product);
                Toast.makeText(FavoriteActivity.this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onItemClick(Product product) {
                Intent intent = new Intent(FavoriteActivity.this, ProductDetailActivity.class);
                intent.putExtra("product_data", product);
                startActivity(intent);
            }
        });
        gvFavorites.setAdapter(adapter);

        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavoritesFromLocal();
    }

    private void loadFavoritesFromLocal() {
        if (MainActivity.currentUser != null) {
            favoriteList.clear();
            List<Product> list = favoriteDAO.getFavorites(MainActivity.currentUser.getEmail());
            if (list != null && !list.isEmpty()) {
                favoriteList.addAll(list);
                tvEmpty.setVisibility(View.GONE);
            } else {
                tvEmpty.setVisibility(View.VISIBLE);
            }
            adapter.notifyDataSetChanged();
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_favorites);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (itemId == R.id.nav_products) {
                startActivity(new Intent(this, ProductListActivity.class));
                return true;
            } else if (itemId == R.id.nav_favorites) {
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }
}
