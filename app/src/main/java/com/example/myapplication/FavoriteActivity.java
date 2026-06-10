package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class FavoriteActivity extends AppCompatActivity {

    private static final String TAG = "FavoriteActivity";
    private GridView gvFavorites;
    private ProductAdapter adapter;
    private List<Product> favoriteList;
    private TextView tvEmpty;
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigationView;
    private FavoriteDAO favoriteDAO;
    private ProductDAO productDAO;

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
        productDAO = new ProductDAO();
        
        gvFavorites = findViewById(R.id.gv_favorites);
        tvEmpty = findViewById(R.id.tv_empty_favorites);
        progressBar = findViewById(R.id.progress_bar_favorite);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        favoriteList = new ArrayList<>();
        adapter = new ProductAdapter(this, favoriteList, new ProductAdapter.OnProductActionListener() {
            @Override public void onEdit(Product product) {}
            @Override public void onDelete(Product product) {}
            @Override public void onBuy(Product product) {
                CartActivity.cartItemList.add(product);
                Toast.makeText(FavoriteActivity.this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }
            @Override public void onItemClick(Product product) {
                Intent intent = new Intent(FavoriteActivity.this, ProductDetailActivity.class);
                intent.putExtra("product_data", product);
                startActivity(intent);
            }
        });
        gvFavorites.setAdapter(adapter);

        setupBottomNavigation();
        
        // Bắt đầu quy trình kiểm tra và tải dữ liệu
        checkAndLoadFavorites();
    }

    private void checkAndLoadFavorites() {
        String email = MainActivity.currentUser.getEmail();
        if (email == null) return;

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        loadFavoritesFromFirebase(email);
    }

    private void loadFavoritesFromFirebase(String email) {
        favoriteDAO.getFavorites(email).addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.isEmpty()) {
                updateUI(new ArrayList<>());
                return;
            }

            List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                String productId = doc.getString("productId");
                if (productId != null) {
                    tasks.add(productDAO.getProductById(productId));
                }
            }

            if (tasks.isEmpty()) {
                updateUI(new ArrayList<>());
                return;
            }

            Tasks.whenAllComplete(tasks).addOnCompleteListener(allTasks -> {
                List<Product> products = new ArrayList<>();
                for (Task<DocumentSnapshot> task : tasks) {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        Product p = task.getResult().toObject(Product.class);
                        if (p != null) {
                            p.setFirebaseId(task.getResult().getId());
                            products.add(p);
                        }
                    }
                }
                updateUI(products);
            });
        });
    }

    private void updateUI(List<Product> products) {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        favoriteList.clear();
        favoriteList.addAll(products);
        adapter.notifyDataSetChanged();
        
        if (favoriteList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("Chưa có sản phẩm yêu thích nào");
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MainActivity.currentUser != null) {
            loadFavoritesFromFirebase(MainActivity.currentUser.getEmail());
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
