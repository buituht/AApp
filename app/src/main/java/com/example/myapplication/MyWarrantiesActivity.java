package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MyWarrantiesActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ListView lvWarranties;
    private TextView tvNoWarranties;
    private List<WarrantyCard> allWarranties = new ArrayList<>();
    private List<WarrantyCard> filteredWarranties = new ArrayList<>();
    private WarrantyAdapter adapter;
    private OrderDAO orderDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_warranties);

        orderDAO = new OrderDAO();
        initViews();
        setupTabs();
        
        if (MainActivity.isLoggedIn && MainActivity.currentUser != null) {
            loadAllPurchasesAsWarranties();
        } else {
            tvNoWarranties.setText("Vui lòng đăng nhập để xem danh sách bảo hành.");
            tvNoWarranties.setVisibility(View.VISIBLE);
        }
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tab_layout_warranty);
        lvWarranties = findViewById(R.id.lv_warranties);
        tvNoWarranties = findViewById(R.id.tv_no_warranties);
        
        adapter = new WarrantyAdapter();
        lvWarranties.setAdapter(adapter);

        findViewById(R.id.btn_back_warranty).setOnClickListener(v -> finish());

        lvWarranties.setOnItemClickListener((parent, view, position, id) -> {
            WarrantyCard card = filteredWarranties.get(position);
            Intent intent = new Intent(this, WarrantyDetailActivity.class);
            intent.putExtra("warranty_card", card);
            startActivity(intent);
        });
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterWarranties(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadAllPurchasesAsWarranties() {
        orderDAO.getOrdersByUsername(MainActivity.currentUser.getUsername()).addOnSuccessListener(queryDocumentSnapshots -> {
            allWarranties.clear();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Order order = doc.toObject(Order.class);
                if (order != null && "Đã giao".equals(order.getStatus())) {
                    if (order.getItems() != null) {
                        for (Product p : order.getItems()) {
                            WarrantyCard card = new WarrantyCard();
                            card.setOrderId(doc.getId());
                            card.setProductId(p.getId());
                            card.setProductName(p.getName());
                            card.setActivationDate(order.getTimestamp());
                            
                            // Lấy số tháng bảo hành từ sản phẩm
                            int months = 12; // Mặc định
                            if (p.getWarranty() != null && !p.getWarranty().isEmpty()) {
                                try {
                                    String digits = p.getWarranty().replaceAll("[^0-9]", "");
                                    if (!digits.isEmpty()) {
                                        months = Integer.parseInt(digits);
                                    }
                                } catch (NumberFormatException e) {
                                    months = 12;
                                }
                            }
                            card.setWarrantyPeriod(months + " tháng");

                            Calendar cal = Calendar.getInstance();
                            cal.setTimeInMillis(order.getTimestamp());
                            cal.add(Calendar.MONTH, months);
                            card.setExpiryDate(cal.getTimeInMillis());
                            
                            if (System.currentTimeMillis() < card.getExpiryDate()) {
                                card.setStatus("Còn hiệu lực");
                            } else {
                                card.setStatus("Hết hạn");
                            }
                            allWarranties.add(card);
                        }
                    }
                }
            }
            filterWarranties(tabLayout.getSelectedTabPosition());
        });
    }

    private void filterWarranties(int tabPosition) {
        filteredWarranties.clear();
        long currentTime = System.currentTimeMillis();
        
        for (WarrantyCard card : allWarranties) {
            boolean isActive = currentTime < card.getExpiryDate();
            if (tabPosition == 0 && isActive) {
                filteredWarranties.add(card);
            } else if (tabPosition == 1 && !isActive) {
                filteredWarranties.add(card);
            }
        }
        
        adapter.notifyDataSetChanged();
        
        if (filteredWarranties.isEmpty()) {
            tvNoWarranties.setText(tabPosition == 0 ? "Không có sản phẩm nào đang bảo hành." : "Không có sản phẩm nào hết hạn bảo hành.");
            tvNoWarranties.setVisibility(View.VISIBLE);
        } else {
            tvNoWarranties.setVisibility(View.GONE);
        }
    }

    private class WarrantyAdapter extends BaseAdapter {
        @Override
        public int getCount() { return filteredWarranties.size(); }
        @Override
        public Object getItem(int position) { return filteredWarranties.get(position); }
        @Override
        public long getItemId(int position) { return position; }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(MyWarrantiesActivity.this).inflate(R.layout.item_warranty_card, parent, false);
            }
            
            WarrantyCard card = filteredWarranties.get(position);
            
            TextView tvProductName = convertView.findViewById(R.id.tv_warranty_product_name);
            TextView tvStatus = convertView.findViewById(R.id.tv_warranty_status);
            TextView tvExpiry = convertView.findViewById(R.id.tv_warranty_expiry);
            TextView tvTimeLeft = convertView.findViewById(R.id.tv_warranty_time_left);
            
            tvProductName.setText(card.getProductName());
            
            // Cập nhật trạng thái hiển thị mượt mà
            if ("Còn hiệu lực".equals(card.getStatus())) {
                tvStatus.setText("Đang bảo hành");
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                
                long diff = card.getExpiryDate() - System.currentTimeMillis();
                long days = TimeUnit.MILLISECONDS.toDays(diff);
                if (tvTimeLeft != null) tvTimeLeft.setText("Còn lại: " + days + " ngày");
            } else {
                tvStatus.setText("Hết hạn");
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                if (tvTimeLeft != null) tvTimeLeft.setText("Đã hết hạn bảo hành");
            }
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            tvExpiry.setText("Hết hạn: " + sdf.format(new Date(card.getExpiryDate())));
            
            return convertView;
        }
    }
}
