package com.example.myapplication;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class HomeCategoryAdapter extends RecyclerView.Adapter<HomeCategoryAdapter.CategoryViewHolder> {

    private List<Category> categoryList;
    private OnCategoryClickListener listener;
    private int selectedPosition = 0;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public HomeCategoryAdapter(List<Category> categoryList, OnCategoryClickListener listener) {
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.tvName.setText(category.getName());
        
        boolean isSelected = position == selectedPosition;
        String imageUrl = category.getImageUrl();
        boolean hasImage = imageUrl != null && !imageUrl.isEmpty();

        // 1. Phản hồi thị giác khi chọn (Selection Visual)
        if (isSelected) {
            holder.cardCategory.setStrokeWidth(4); // Viền màu tím
            holder.cardCategory.setStrokeColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.purple_main));
            holder.cardCategory.setCardElevation(8f); // Đổ bóng cao hơn
            holder.tvName.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.purple_main));
            holder.tvName.setTypeface(null, Typeface.BOLD);
        } else {
            holder.cardCategory.setStrokeWidth(0);
            holder.cardCategory.setCardElevation(2f);
            holder.tvName.setTextColor(Color.parseColor("#424242"));
            holder.tvName.setTypeface(null, Typeface.NORMAL);
        }

        // 2. Xử lý hiển thị hình ảnh
        if (hasImage) {
            holder.ivImage.setPadding(0, 0, 0, 0); // Không padding cho ảnh thực tế
            holder.ivImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            holder.ivImage.clearColorFilter();
            
            Glide.with(holder.itemView.getContext())
                    .load(GlideUtils.getGlideUrlWithUserAgent(imageUrl))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.ic_ball)
                    .error(R.drawable.ic_ball)
                    .circleCrop()
                    .into(holder.ivImage);
        } else {
            // Hiển thị icon mặc định
            holder.ivImage.setPadding(32, 32, 32, 32); 
            holder.ivImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            if (category.getName().equals("Tất cả")) {
                holder.ivImage.setImageResource(android.R.drawable.ic_menu_sort_by_size);
            } else {
                holder.ivImage.setImageResource(R.drawable.ic_ball);
            }

            // Đổi màu icon dựa trên trạng thái chọn
            if (isSelected) {
                holder.ivImage.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.purple_main));
            } else {
                holder.ivImage.setColorFilter(Color.parseColor("#757575"));
            }
        }

        // 3. Sự kiện nhấn
        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedPosition);
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardCategory;
        ImageView ivImage;
        TextView tvName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            // SỬA LỖI: Tìm ID chính xác từ XML
            cardCategory = itemView.findViewById(R.id.card_category);
            ivImage = itemView.findViewById(R.id.iv_category_icon);
            tvName = itemView.findViewById(R.id.tv_category_name);
        }
    }
}
