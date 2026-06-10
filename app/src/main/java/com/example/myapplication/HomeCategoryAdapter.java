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

        // 1. Hiệu ứng Selection (Sang trọng & Nhẹ nhàng)
        if (isSelected) {
            holder.cardCategory.setStrokeWidth(3); // Viền vàng mảnh
            holder.cardCategory.setStrokeColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.purple_main));
            holder.cardCategory.setCardElevation(6f); // Đổ bóng nhẹ
            holder.cardCategory.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.purple_light));
            holder.tvName.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.purple_main));
            holder.tvName.setTypeface(null, Typeface.BOLD);
        } else {
            holder.cardCategory.setStrokeWidth(0);
            holder.cardCategory.setCardElevation(2f);
            holder.cardCategory.setCardBackgroundColor(Color.WHITE);
            holder.tvName.setTextColor(Color.parseColor("#757575"));
            holder.tvName.setTypeface(null, Typeface.NORMAL);
        }

        // 2. Xử lý hiển thị hình ảnh/icon
        if (hasImage) {
            holder.ivImage.setPadding(0, 0, 0, 0); 
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
            // Icon mặc định với padding hợp lý (không quá nhỏ)
            holder.ivImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            holder.ivImage.setPadding(32, 32, 32, 32); 
            
            if (category.getName().equalsIgnoreCase("Tất cả")) {
                holder.ivImage.setImageResource(android.R.drawable.ic_menu_sort_by_size);
            } else {
                holder.ivImage.setImageResource(R.drawable.ic_ball);
            }

            // Đổi màu icon theo trạng thái chọn (vàng gold hoặc xám)
            if (isSelected) {
                holder.ivImage.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.purple_main));
            } else {
                holder.ivImage.setColorFilter(Color.parseColor("#BDBDBD"));
            }
        }

        // 3. Sự kiện nhấn
        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            if (previousSelected != selectedPosition) {
                notifyItemChanged(previousSelected);
                notifyItemChanged(selectedPosition);
            }
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
            cardCategory = itemView.findViewById(R.id.card_category);
            ivImage = itemView.findViewById(R.id.iv_category_icon);
            tvName = itemView.findViewById(R.id.tv_category_name);
        }
    }
}
