package com.example.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WarrantyClaimAdminAdapter extends RecyclerView.Adapter<WarrantyClaimAdminAdapter.ViewHolder> {

    private Context context;
    private List<WarrantyClaim> claimList;
    private OnClaimUpdateListener listener;

    public interface OnClaimUpdateListener {
        void onUpdateStatus(WarrantyClaim claim);
    }

    public void setOnClaimUpdateListener(OnClaimUpdateListener listener) {
        this.listener = listener;
    }

    public WarrantyClaimAdminAdapter(Context context, List<WarrantyClaim> claimList) {
        this.context = context;
        this.claimList = claimList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_warranty_claim_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WarrantyClaim claim = claimList.get(position);
        holder.tvProductName.setText(claim.getProductName());
        holder.tvUserEmail.setText("Người dùng: " + claim.getUserEmail());
        holder.tvDescription.setText(claim.getDescription());
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.tvDate.setText("Ngày gửi: " + sdf.format(new Date(claim.getTimestamp())));
        
        holder.tvStatus.setText(claim.getStatus());
        setStatusStyle(holder.tvStatus, claim.getStatus());

        holder.btnUpdateStatus.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUpdateStatus(claim);
            }
        });
    }

    private void setStatusStyle(TextView tvStatus, String status) {
        if (status == null) return;
        
        switch (status) {
            case "Chờ xử lý":
            case "Pending":
                tvStatus.setTextColor(Color.parseColor("#FFA500")); // Orange
                break;
            case "Đang xử lý":
            case "Processing":
                tvStatus.setTextColor(Color.BLUE);
                break;
            case "Đã giải quyết":
            case "Resolved":
                tvStatus.setTextColor(Color.parseColor("#006400")); // Dark Green
                break;
            case "Từ chối":
            case "Rejected":
                tvStatus.setTextColor(Color.RED);
                break;
            default:
                tvStatus.setTextColor(Color.BLACK);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return claimList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvUserEmail, tvDescription, tvDate, tvStatus;
        Button btnUpdateStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tv_claim_product_name);
            tvUserEmail = itemView.findViewById(R.id.tv_claim_user_email);
            tvDescription = itemView.findViewById(R.id.tv_claim_description);
            tvDate = itemView.findViewById(R.id.tv_claim_date);
            tvStatus = itemView.findViewById(R.id.tv_claim_status);
            btnUpdateStatus = itemView.findViewById(R.id.btn_update_status);
        }
    }
}
