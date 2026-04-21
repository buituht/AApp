package com.example.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ReportActivity extends AppCompatActivity {

    private TextView tvTotalRevenue, tvCustomerCount, tvOrderCount;
    private TextView tvProcessing, tvShipping, tvCompleted, tvCancelled;
    private ImageButton btnBack;
    private PieChartView pieChart;
    private FirebaseFirestore db;
    private ListenerRegistration userListener, orderListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        db = FirebaseFirestore.getInstance();
        initViews();
        startRealtimeUpdates();
        
        findViewById(R.id.btn_refresh_report).setOnClickListener(v -> {
            Toast.makeText(this, "Đang cập nhật dữ liệu mới nhất...", Toast.LENGTH_SHORT).show();
        });
    }

    private void initViews() {
        tvTotalRevenue = findViewById(R.id.tv_report_total_revenue);
        tvCustomerCount = findViewById(R.id.tv_report_customer_count);
        tvOrderCount = findViewById(R.id.tv_report_order_count);
        
        tvProcessing = findViewById(R.id.tv_status_processing);
        tvShipping = findViewById(R.id.tv_status_shipping);
        tvCompleted = findViewById(R.id.tv_status_completed);
        tvCancelled = findViewById(R.id.tv_status_cancelled);
        
        pieChart = findViewById(R.id.pie_chart_orders);
        
        btnBack = findViewById(R.id.btn_back_report);
        btnBack.setOnClickListener(v -> finish());
    }

    private void startRealtimeUpdates() {
        // 1. Tự động cập nhật số lượng khách hàng
        userListener = db.collection("users").addSnapshotListener((userSnapshots, e) -> {
            if (e != null) {
                Toast.makeText(this, "Lỗi đồng bộ người dùng", Toast.LENGTH_SHORT).show();
                return;
            }
            if (userSnapshots != null) {
                int customerCount = 0;
                for (QueryDocumentSnapshot doc : userSnapshots) {
                    String username = doc.getString("username");
                    if (username != null && !username.equals("admin")) {
                        customerCount++;
                    }
                }
                tvCustomerCount.setText(String.valueOf(customerCount));
            }
        });

        // 2. Tự động cập nhật thống kê đơn hàng và doanh thu
        orderListener = db.collection("orders").addSnapshotListener((orderSnapshots, e) -> {
            if (e != null) {
                Toast.makeText(this, "Lỗi đồng bộ đơn hàng", Toast.LENGTH_SHORT).show();
                return;
            }
            if (orderSnapshots != null) {
                int totalOrders = orderSnapshots.size();
                long totalRevenue = 0;
                int countProcessing = 0;
                int countShipping = 0;
                int countCompleted = 0;
                int countCancelled = 0;

                for (QueryDocumentSnapshot doc : orderSnapshots) {
                    Long totalPrice = doc.getLong("totalPrice");
                    String status = doc.getString("status");

                    if (status != null) {
                        switch (status) {
                            case "Đang xử lý":
                                countProcessing++;
                                break;
                            case "Đang giao":
                                countShipping++;
                                break;
                            case "Đã giao":
                                countCompleted++;
                                if (totalPrice != null) {
                                    totalRevenue += totalPrice;
                                }
                                break;
                            case "Đã hủy":
                                countCancelled++;
                                break;
                        }
                    }
                }

                tvOrderCount.setText(String.valueOf(totalOrders));
                tvTotalRevenue.setText(String.format("%,d VNĐ", totalRevenue));

                tvProcessing.setText(String.valueOf(countProcessing));
                tvShipping.setText(String.valueOf(countShipping));
                tvCompleted.setText(String.valueOf(countCompleted));
                tvCancelled.setText(String.valueOf(countCancelled));
                
                updateChart(countProcessing, countShipping, countCompleted, countCancelled);
            }
        });
    }

    private void updateChart(int processing, int shipping, int completed, int cancelled) {
        List<PieChartView.Slice> slices = new ArrayList<>();
        if (processing > 0) slices.add(new PieChartView.Slice(processing, Color.parseColor("#2196F3"), "Xử lý"));
        if (shipping > 0) slices.add(new PieChartView.Slice(shipping, Color.parseColor("#FFC107"), "Giao"));
        if (completed > 0) slices.add(new PieChartView.Slice(completed, Color.parseColor("#4CAF50"), "Xong"));
        if (cancelled > 0) slices.add(new PieChartView.Slice(cancelled, Color.parseColor("#F44336"), "Hủy"));
        
        if (slices.isEmpty()) {
            slices.add(new PieChartView.Slice(1, Color.LTGRAY, "Trống"));
        }
        
        pieChart.setData(slices);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userListener != null) userListener.remove();
        if (orderListener != null) orderListener.remove();
    }
}
