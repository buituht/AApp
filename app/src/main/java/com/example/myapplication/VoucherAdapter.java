package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.ViewHolder> {

    private List<Voucher> vouchers;
    private OnVoucherActionListener listener;
    private OnVoucherSelectedListener selectListener;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public interface OnVoucherActionListener {
        void onEdit(Voucher voucher);
        void onDelete(Voucher voucher);
    }

    public interface OnVoucherSelectedListener {
        void onSelect(Voucher voucher);
    }

    public VoucherAdapter(List<Voucher> vouchers, OnVoucherActionListener listener) {
        this.vouchers = vouchers;
        this.listener = listener;
    }

    public VoucherAdapter(Context context, List<Voucher> vouchers, OnVoucherSelectedListener listener) {
        this.vouchers = vouchers;
        this.selectListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_voucher_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Voucher voucher = vouchers.get(position);
        holder.tvCode.setText(voucher.getCode());
        holder.tvDesc.setText(voucher.getDescription());
        
        String valueStr = voucher.getDiscountValue() + (voucher.getType().equals("PERCENT") ? "%" : " VNĐ");
        holder.tvValue.setText("Giảm: " + valueStr + " | Tối thiểu: " + String.format("%,d VNĐ", voucher.getMinOrderAmount()));

        String dateRange = "Hạn dùng: " + sdf.format(new Date(voucher.getStartDate())) + " - " + sdf.format(new Date(voucher.getExpiryDate()));
        holder.tvExpiry.setText(dateRange);

        if (listener != null) {
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnEdit.setOnClickListener(v -> listener.onEdit(voucher));
            holder.btnDelete.setOnClickListener(v -> listener.onDelete(voucher));
        } else {
            holder.btnEdit.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
        }

        if (selectListener != null) {
            holder.itemView.setOnClickListener(v -> selectListener.onSelect(voucher));
        }
    }

    @Override
    public int getItemCount() {
        return vouchers.size();
    }

    public void updateData(List<Voucher> newVouchers) {
        this.vouchers = newVouchers;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCode, tvDesc, tvValue, tvExpiry;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tv_voucher_code);
            tvDesc = itemView.findViewById(R.id.tv_voucher_desc);
            tvValue = itemView.findViewById(R.id.tv_voucher_details);
            tvExpiry = itemView.findViewById(R.id.tv_voucher_expiry);
            btnEdit = itemView.findViewById(R.id.btn_edit_voucher);
            btnDelete = itemView.findViewById(R.id.btn_delete_voucher);
        }
    }
}
