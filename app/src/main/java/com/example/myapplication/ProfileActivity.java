package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvName, tvPhone, tvAddress, tvHomeAddress, tvCompanyAddress, tvGender, tvDob, tvNoOrders;
    private TextView tvLoyaltyPoints, tvTotalSpent, tvMembershipLevel;
    private Button btnLogout;
    private ImageView btnEditProfile, ivAvatar;
    private TextView tvChangePassword;
    private NonScrollListView lvOrders;
    private BottomNavigationView bottomNavigationView;
    private DatabaseHelper dbHelper;
    private List<Order> orderList = new ArrayList<>();
    private OrderAdapter orderAdapter;

    // Admin buttons
    private LinearLayout layoutAdminPanel;
    private Button btnManageProducts, btnManageCategories, btnManageBanners, btnManageOrders, btnManageFaq, btnViewReports, btnManageVouchers;
    private TextView tvLabelOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Kiểm tra đăng nhập
        if (!MainActivity.isLoggedIn) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        dbHelper = new DatabaseHelper(this);
        initViews();
        displayUserInfo();
        setupBottomNavigation();
        setupClickListeners();
        loadOrders();
        updateAdminVisibility();
    }

    private void initViews() {
        tvName = findViewById(R.id.tv_profile_name);
        tvPhone = findViewById(R.id.tv_profile_phone);
        tvAddress = findViewById(R.id.tv_profile_address);
        tvHomeAddress = findViewById(R.id.tv_profile_home_address);
        tvCompanyAddress = findViewById(R.id.tv_profile_company_address);
        tvGender = findViewById(R.id.tv_profile_gender);
        tvDob = findViewById(R.id.tv_profile_dob);
        ivAvatar = findViewById(R.id.iv_profile_avatar);
        tvChangePassword = findViewById(R.id.tv_change_password);
        tvNoOrders = findViewById(R.id.tv_no_orders);
        lvOrders = findViewById(R.id.lv_profile_orders);
        tvLabelOrders = findViewById(R.id.tv_label_orders);
        
        tvLoyaltyPoints = findViewById(R.id.tv_loyalty_points);
        tvTotalSpent = findViewById(R.id.tv_total_spent);
        tvMembershipLevel = findViewById(R.id.tv_membership_level);
        
        btnLogout = findViewById(R.id.btn_logout_profile);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Admin Panel
        layoutAdminPanel = findViewById(R.id.layout_admin_panel);
        btnManageProducts = findViewById(R.id.btn_manage_products);
        btnManageCategories = findViewById(R.id.btn_manage_categories);
        btnManageBanners = findViewById(R.id.btn_manage_banners);
        btnManageOrders = findViewById(R.id.btn_manage_orders);
        btnManageFaq = findViewById(R.id.btn_manage_faq);
        btnViewReports = findViewById(R.id.btn_view_reports);
        btnManageVouchers = findViewById(R.id.btn_manage_vouchers);

        orderAdapter = new OrderAdapter(this, orderList);
        orderAdapter.setOnOrderUpdateListener(this::showUpdateStatusDialog);
        lvOrders.setAdapter(orderAdapter);
    }

    private void showUpdateStatusDialog(Order order) {
        String[] statuses = {"Đang xử lý", "Đang giao", "Đã giao", "Đã hủy"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cập nhật trạng thái đơn hàng");
        builder.setItems(statuses, (dialog, which) -> {
            String newStatus = statuses[which];
            updateOrderStatus(order, newStatus);
        });
        builder.show();
    }

    private void updateOrderStatus(Order order, String newStatus) {
        if (order.getOrderId() == null) return;
        
        order.setStatus(newStatus);
        // dbHelper.updateOrderStatus(order.getOrderId(), newStatus);
        
        Toast.makeText(this, "Cập nhật trạng thái thành công", Toast.LENGTH_SHORT).show();
        loadOrders();
    }

    private void updateAdminVisibility() {
        if (MainActivity.isAdmin) {
            layoutAdminPanel.setVisibility(View.VISIBLE);
            tvLabelOrders.setVisibility(View.GONE);
            lvOrders.setVisibility(View.GONE);
            tvNoOrders.setVisibility(View.GONE);
            findViewById(R.id.card_loyalty).setVisibility(View.GONE);
            tvMembershipLevel.setVisibility(View.GONE);
        } else {
            layoutAdminPanel.setVisibility(View.GONE);
            tvLabelOrders.setVisibility(View.VISIBLE);
            tvLabelOrders.setText("ĐƠN HÀNG CỦA TÔI");
            lvOrders.setVisibility(View.VISIBLE);
            findViewById(R.id.card_loyalty).setVisibility(View.VISIBLE);
            tvMembershipLevel.setVisibility(View.VISIBLE);
        }
    }

    private void loadOrders() {
        User user = MainActivity.currentUser;
        if (user == null || MainActivity.isAdmin) return;

        orderList.clear();
        orderList.addAll(dbHelper.getOrdersByUsername(user.getUsername()));
        orderAdapter.notifyDataSetChanged();
        tvNoOrders.setVisibility(orderList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void setupClickListeners() {
        btnLogout.setOnClickListener(v -> {
            MainActivity.isLoggedIn = false;
            MainActivity.isAdmin = false;
            MainActivity.currentUser = null;
            Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        btnEditProfile.setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));
        tvChangePassword.setOnClickListener(v -> startActivity(new Intent(this, ChangePasswordActivity.class)));

        // Admin Click Listeners
        btnManageProducts.setOnClickListener(v -> startActivity(new Intent(this, ProductAdminActivity.class)));
        btnManageCategories.setOnClickListener(v -> startActivity(new Intent(this, AddCategoryActivity.class)));
        btnManageBanners.setOnClickListener(v -> startActivity(new Intent(this, BannerAdminActivity.class)));
        btnManageFaq.setOnClickListener(v -> startActivity(new Intent(this, FaqAdminActivity.class)));
        btnManageOrders.setOnClickListener(v -> startActivity(new Intent(this, OrderAdminActivity.class)));
        btnManageVouchers.setOnClickListener(v -> startActivity(new Intent(this, VoucherAdminActivity.class)));
        
        btnViewReports.setOnClickListener(v -> startActivity(new Intent(this, ReportActivity.class)));
    }

    private void displayUserInfo() {
        User user = MainActivity.currentUser;
        if (user != null) {
            tvName.setText(user.getFullName());
            tvPhone.setText("Số điện thoại: " + (user.getPhoneNumber() != null ? user.getPhoneNumber() : "Chưa cập nhật"));
            tvAddress.setText("Địa chỉ: " + (user.getAddress() != null ? user.getAddress() : "Chưa cập nhật"));
            tvHomeAddress.setText("Địa chỉ nhà: " + (user.getHomeAddress() != null ? user.getHomeAddress() : "Chưa cập nhật"));
            tvCompanyAddress.setText("Địa chỉ công ty: " + (user.getCompanyAddress() != null ? user.getCompanyAddress() : "Chưa cập nhật"));
            tvGender.setText("Giới tính: " + (user.getGender() != null ? user.getGender() : "Chưa cập nhật"));
            tvDob.setText("Ngày sinh: " + (user.getDob() != null ? user.getDob() : "Chưa cập nhật"));

            // Loyalty info
            tvLoyaltyPoints.setText(String.valueOf(user.getPoints()));
            tvTotalSpent.setText(String.format(Locale.getDefault(), "%,d VNĐ", user.getTotalSpent()));
            
            updateMembershipBadge(user);

            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                Glide.with(this)
                        .load(GlideUtils.getGlideUrlWithUserAgent(user.getAvatarUrl()))
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .circleCrop()
                        .into(ivAvatar);
            }
        }
    }

    private void updateMembershipBadge(User user) {
        String level = user.getMembershipLevel();
        tvMembershipLevel.setText(level);
        
        int backgroundRes;
        int textColor = Color.BLACK;

        switch (level) {
            case "Kim cương":
                backgroundRes = R.drawable.badge_diamond;
                textColor = Color.parseColor("#006064");
                break;
            case "Vàng":
                backgroundRes = R.drawable.badge_gold;
                textColor = Color.parseColor("#F57F17");
                break;
            case "Bạc":
                backgroundRes = R.drawable.badge_silver;
                textColor = Color.parseColor("#455A64");
                break;
            default: // Đồng
                backgroundRes = R.drawable.badge_bronze;
                textColor = Color.parseColor("#5D4037");
                break;
        }

        tvMembershipLevel.setBackgroundResource(backgroundRes);
        tvMembershipLevel.setTextColor(textColor);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!MainActivity.isLoggedIn) {
            finish();
            return;
        }
        displayUserInfo();
        updateAdminVisibility();
        loadOrders();
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
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
            }
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
                return true;
            }
            return false;
        });
    }
}
