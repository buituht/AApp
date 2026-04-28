package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminWarrantyLookupActivity extends AppCompatActivity {

    private EditText etOrderId;
    private Button btnSearch;
    private ListView lvResults;
    private TextView tvNoResults;
    private List<WarrantyCard> resultsList = new ArrayList<>();
    private WarrantyAdapter adapter;
    private OrderDAO orderDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_warranty_lookup);

        Toolbar toolbar = findViewById(R.id.toolbar_lookup);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        orderDAO = new OrderDAO();
        initViews();
    }

    private void initViews() {
        etOrderId = findViewById(R.id.et_lookup_order_id);
        btnSearch = findViewById(R.id.btn_lookup_search);
        lvResults = findViewById(R.id.lv_lookup_results);
        tvNoResults = findViewById(R.id.tv_no_results);

        adapter = new WarrantyAdapter();
        lvResults.setAdapter(adapter);

        btnSearch.setOnClickListener(v -> {
            String orderId = etOrderId.getText().toString().trim();
            if (orderId.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mã đơn hàng", Toast.LENGTH_SHORT).show();
                return;
            }
            performLookup(orderId);
        });
    }

    private void performLookup(String orderId) {
        orderDAO.getOrderById(orderId).addOnSuccessListener(documentSnapshot -> {
            resultsList.clear();
            Order order = documentSnapshot.toObject(Order.class);
            if (order != null) {
                if (order.getItems() != null) {
                    for (Product p : order.getItems()) {
                        WarrantyCard card = new WarrantyCard();
                        card.setOrderId(documentSnapshot.getId());
                        card.setProductId(p.getId());
                        card.setProductName(p.getName());
                        card.setActivationDate(order.getTimestamp());
                        card.setUserEmail(order.getUsername()); // Giả định username là email hoặc định danh

                        // Lấy số tháng bảo hành từ sản phẩm
                        int months = 12;
                        if (p.getWarranty() != null && !p.getWarranty().isEmpty()) {
                            try {
                                String digits = p.getWarranty().replaceAll("[^0-9]", "");
                                if (!digits.isEmpty()) months = Integer.parseInt(digits);
                            } catch (Exception e) { months = 12; }
                        }
                        
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(order.getTimestamp());
                        cal.add(Calendar.MONTH, months);
                        card.setExpiryDate(cal.getTimeInMillis());
                        card.setWarrantyPeriod(months + " tháng");
                        
                        resultsList.add(card);
                    }
                }
            }

            adapter.notifyDataSetChanged();
            if (resultsList.isEmpty()) {
                tvNoResults.setText("Không tìm thấy thông tin bảo hành cho đơn hàng này.");
                tvNoResults.setVisibility(View.VISIBLE);
            } else {
                tvNoResults.setVisibility(View.GONE);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class WarrantyAdapter extends BaseAdapter {
        @Override
        public int getCount() { return resultsList.size(); }
        @Override
        public Object getItem(int position) { return resultsList.get(position); }
        @Override
        public long getItemId(int position) { return position; }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(AdminWarrantyLookupActivity.this).inflate(R.layout.item_warranty_card, parent, false);
            }
            WarrantyCard card = resultsList.get(position);
            TextView tvName = convertView.findViewById(R.id.tv_warranty_product_name);
            TextView tvExpiry = convertView.findViewById(R.id.tv_warranty_expiry);
            TextView tvStatus = convertView.findViewById(R.id.tv_warranty_status);
            
            tvName.setText(card.getProductName());
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            tvExpiry.setText("Hết hạn: " + sdf.format(new Date(card.getExpiryDate())));
            
            if (System.currentTimeMillis() < card.getExpiryDate()) {
                tvStatus.setText("Đang bảo hành");
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                tvStatus.setText("Hết hạn");
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
            return convertView;
        }
    }
}
