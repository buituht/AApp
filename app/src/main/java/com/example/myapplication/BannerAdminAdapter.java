package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class BannerAdminAdapter extends RecyclerView.Adapter<BannerAdminAdapter.ViewHolder> {

    private List<Banner> bannerList;
    private OnBannerActionListener listener;

    public interface OnBannerActionListener {
        void onEdit(Banner banner);
        void onDelete(Banner banner);
    }

    public BannerAdminAdapter(List<Banner> bannerList, OnBannerActionListener listener) {
        this.bannerList = bannerList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Banner banner = bannerList.get(position);
        holder.tvUrl.setText(banner.getImageUrl());
        Glide.with(holder.itemView.getContext())
                .load(GlideUtils.getGlideUrlWithUserAgent(banner.getImageUrl()))
                .placeholder(R.drawable.ic_ball)
                .into(holder.ivBanner);

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(banner));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(banner));
    }

    @Override
    public int getItemCount() {
        return bannerList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBanner;
        TextView tvUrl;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBanner = itemView.findViewById(R.id.iv_banner_admin);
            tvUrl = itemView.findViewById(R.id.tv_banner_url);
            btnEdit = itemView.findViewById(R.id.btn_edit_banner);
            btnDelete = itemView.findViewById(R.id.btn_delete_banner);
        }
    }
}
