package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class OrderListFragment extends Fragment {

    private String status;
    private RecyclerView rvOrders;
    private TextView tvNoOrders;
    private FirebaseFirestore db;
    private List<Order> orderList = new ArrayList<>();
    private OrderAdminAdapter adapter;

    public static OrderListFragment newInstance(String status) {
        OrderListFragment fragment = new OrderListFragment();
        Bundle args = new Bundle();
        args.putString("status", status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            status = getArguments().getString("status");
        }
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_list, container, false);
        rvOrders = view.findViewById(R.id.rv_orders);
        tvNoOrders = view.findViewById(R.id.tv_no_orders);

        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new OrderAdminAdapter(getContext(), orderList);
        
        // Listener để cập nhật trạng thái khi admin click vào item
        adapter.setOnOrderUpdateListener(this::showUpdateStatusDialog);
        
        rvOrders.setAdapter(adapter);

        loadOrders();
        return view;
    }

    private void loadOrders() {
        Query query = db.collection("orders").orderBy("timestamp", Query.Direction.DESCENDING);
        
        // Nếu không phải tab "Tất cả", thêm điều kiện lọc theo status
        if (status != null && !status.equals("Tất cả")) {
            query = query.whereEqualTo("status", status);
        }

        query.addSnapshotListener((value, error) -> {
            if (!isAdded()) return; // Tránh leak hoặc lỗi khi fragment đã bị hủy
            
            if (error != null) {
                Toast.makeText(getContext(), "Lỗi tải đơn hàng: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (value != null) {
                orderList.clear();
                orderList.addAll(value.toObjects(Order.class));
                // Gán ID document cho mỗi order object (quan trọng để update)
                for (int i = 0; i < value.getDocuments().size(); i++) {
                    orderList.get(i).setOrderId(value.getDocuments().get(i).getId());
                }
                adapter.notifyDataSetChanged();
                tvNoOrders.setVisibility(orderList.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void showUpdateStatusDialog(Order order) {
        String[] statuses = {"Đang xử lý", "Đang giao", "Đã giao", "Đã hủy"};
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Cập nhật trạng thái đơn hàng");
        
        // Tìm vị trí hiện tại của status để highlight (tùy chọn)
        int checkedItem = -1;
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equals(order.getStatus())) {
                checkedItem = i;
                break;
            }
        }

        builder.setSingleChoiceItems(statuses, checkedItem, (dialog, which) -> {
            String newStatus = statuses[which];
            updateOrderStatus(order, newStatus);
            dialog.dismiss();
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void updateOrderStatus(Order order, String newStatus) {
        if (order.getOrderId() == null) {
            Toast.makeText(getContext(), "Không tìm thấy ID đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        
        db.collection("orders").document(order.getOrderId())
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Cập nhật trạng thái '" + newStatus + "' thành công", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
