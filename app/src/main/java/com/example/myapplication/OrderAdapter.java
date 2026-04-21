package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends BaseAdapter {
    private Context context;
    private List<Order> orderList;
    private OnOrderUpdateListener listener;

    public interface OnOrderUpdateListener {
        void onUpdateStatus(Order order);
    }

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    public void setOnOrderUpdateListener(OnOrderUpdateListener listener) {
        this.listener = listener;
    }

    @Override
    public int getCount() { return orderList.size(); }
    @Override
    public Object getItem(int position) { return orderList.get(position); }
    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        }

        Order order = orderList.get(position);
        TextView tvId = convertView.findViewById(R.id.tv_order_id);
        TextView tvStatus = convertView.findViewById(R.id.tv_order_status);
        TextView tvItems = convertView.findViewById(R.id.tv_order_items);
        TextView tvTotal = convertView.findViewById(R.id.tv_order_total);
        TextView tvDate = convertView.findViewById(R.id.tv_order_date);
        Button btnUpdateStatus = convertView.findViewById(R.id.btn_update_status);

        tvId.setText("Mã ĐH: #" + (order.getOrderId() != null && order.getOrderId().length() > 8 ? order.getOrderId().substring(0, 8).toUpperCase() : order.getOrderId()));
        tvStatus.setText(order.getStatus());
        
        StringBuilder itemsText = new StringBuilder("Sản phẩm: ");
        if (order.getItems() != null) {
            for (int i = 0; i < order.getItems().size(); i++) {
                itemsText.append(order.getItems().get(i).getName());
                if (i < order.getItems().size() - 1) itemsText.append(", ");
            }
        }
        tvItems.setText(itemsText.toString());
        
        tvTotal.setText(String.format("Tổng tiền: %,d VNĐ", order.getTotalPrice()));
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        tvDate.setText("Ngày đặt: " + sdf.format(new Date(order.getTimestamp())));

        if (MainActivity.isAdmin) {
            btnUpdateStatus.setVisibility(View.VISIBLE);
            btnUpdateStatus.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUpdateStatus(order);
                }
            });
        } else {
            btnUpdateStatus.setVisibility(View.GONE);
        }

        return convertView;
    }
}
