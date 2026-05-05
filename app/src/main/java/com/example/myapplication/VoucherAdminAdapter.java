package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VoucherAdminAdapter extends BaseAdapter {
    private Context context;
    private List<Voucher> voucherList;
    private OnVoucherActionListener listener;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public interface OnVoucherActionListener {
        void onEdit(Voucher voucher);
        void onDelete(Voucher voucher);
    }

    public VoucherAdminAdapter(Context context, List<Voucher> voucherList, OnVoucherActionListener listener) {
        this.context = context;
        this.voucherList = voucherList;
        this.listener = listener;
    }

    @Override
    public int getCount() { return voucherList.size(); }
    @Override
    public Object getItem(int position) { return voucherList.get(position); }
    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_voucher_admin, parent, false);
        }

        Voucher voucher = voucherList.get(position);

        TextView tvCode = convertView.findViewById(R.id.tv_voucher_code);
        TextView tvDesc = convertView.findViewById(R.id.tv_voucher_desc);
        TextView tvDetails = convertView.findViewById(R.id.tv_voucher_details);
        TextView tvExpiry = convertView.findViewById(R.id.tv_voucher_expiry);
        ImageButton btnEdit = convertView.findViewById(R.id.btn_edit_voucher);
        ImageButton btnDelete = convertView.findViewById(R.id.btn_delete_voucher);

        tvCode.setText(voucher.getCode());
        tvDesc.setText(voucher.getDescription());
        
        String details = "Giảm: " + voucher.getDiscountValue() + (voucher.getType().equals("PERCENT") ? "%" : " VNĐ") 
                + " | Tối thiểu: " + String.format("%,d VNĐ", voucher.getMinOrderAmount());
        tvDetails.setText(details);
        
        String dateRange = "Hạn dùng: " + sdf.format(new Date(voucher.getStartDate())) + " - " + sdf.format(new Date(voucher.getExpiryDate()));
        tvExpiry.setText(dateRange);

        btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(voucher);
        });

        btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(voucher);
        });

        return convertView;
    }
}
