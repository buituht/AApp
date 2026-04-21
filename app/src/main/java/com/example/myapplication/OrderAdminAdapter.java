package com.example.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdminAdapter extends RecyclerView.Adapter<OrderAdminAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;
    private OnOrderUpdateListener listener;

    public interface OnOrderUpdateListener {
        void onUpdateStatus(Order order);
    }

    public void setOnOrderUpdateListener(OnOrderUpdateListener listener) {
        this.listener = listener;
    }

    public OrderAdminAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        
        holder.tvOrderId.setText("Mã ĐH: " + (order.getOrderId() != null ? (order.getOrderId().length() > 8 ? order.getOrderId().substring(0, 8) : order.getOrderId()) : "N/A"));
        holder.tvOrderDate.setText(formatDate(order.getTimestamp()));
        
        // SỬA LỖI TẠI ĐÂY: Sử dụng %,d thay vì %,.0f vì totalPrice là kiểu long
        holder.tvOrderTotal.setText(String.format("%,dđ", order.getTotalPrice()));
        
        holder.tvOrderStatus.setText(order.getStatus());
        holder.tvUsername.setText("Khách hàng: " + order.getUsername());

        // Set status color
        if ("Đang xử lý".equals(order.getStatus())) holder.tvOrderStatus.setTextColor(Color.parseColor("#FFA500"));
        else if ("Đang giao".equals(order.getStatus())) holder.tvOrderStatus.setTextColor(Color.BLUE);
        else if ("Đã giao".equals(order.getStatus())) holder.tvOrderStatus.setTextColor(Color.parseColor("#008000"));
        else if ("Đã hủy".equals(order.getStatus())) holder.tvOrderStatus.setTextColor(Color.RED);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onUpdateStatus(order);
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderDate, tvOrderTotal, tvOrderStatus, tvUsername;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvOrderDate = itemView.findViewById(R.id.tv_order_date);
            tvOrderTotal = itemView.findViewById(R.id.tv_order_total);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status);
            tvUsername = itemView.findViewById(R.id.tv_order_username);
        }
    }
}
