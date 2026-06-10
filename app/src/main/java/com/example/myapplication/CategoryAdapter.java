package com.example.myapplication;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categoryList;
    private OnCategoryActionListener listener;

    public interface OnCategoryActionListener {
        void onEdit(Category category);
        void onDelete(Category category);
    }

    public CategoryAdapter(List<Category> categoryList, OnCategoryActionListener listener) {
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_manage, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        
        boolean isChild = category.getParentId() != null && !category.getParentId().isEmpty();

        // Cài đặt thụt lề (Indentation) trực quan
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.ivImage.getLayoutParams();
        if (isChild) {
            // Danh mục con: Thụt lề vào 40dp và làm mờ nhẹ
            int marginStart = (int) (40 * holder.itemView.getContext().getResources().getDisplayMetrics().density);
            params.setMargins(marginStart, 0, 0, 0);
            
            holder.tvName.setText("└── " + category.getName());
            holder.tvName.setTypeface(null, Typeface.NORMAL);
            holder.tvName.setTextColor(Color.parseColor("#757575")); // Màu xám cho danh mục con
            holder.cardView.setCardBackgroundColor(Color.parseColor("#FAFAFA")); // Nền hơi xám nhẹ
            holder.cardView.setCardElevation(1f);
        } else {
            // Danh mục cha: Sát lề trái, in đậm
            params.setMargins(0, 0, 0, 0);
            
            holder.tvName.setText(category.getName().toUpperCase());
            holder.tvName.setTypeface(null, Typeface.BOLD);
            holder.tvName.setTextColor(Color.parseColor("#212121"));
            holder.cardView.setCardBackgroundColor(Color.WHITE);
            holder.cardView.setCardElevation(4f);
        }
        holder.ivImage.setLayoutParams(params);
        
        Glide.with(holder.itemView.getContext())
                .load(GlideUtils.getGlideUrlWithUserAgent(category.getImageUrl()))
                .placeholder(R.drawable.ic_ball)
                .into(holder.ivImage);

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(category));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(category));
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivImage;
        TextView tvName;
        ImageButton btnEdit, btnDelete;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            ivImage = itemView.findViewById(R.id.iv_item_cat_image);
            tvName = itemView.findViewById(R.id.tv_item_cat_name);
            btnEdit = itemView.findViewById(R.id.btn_item_cat_edit);
            btnDelete = itemView.findViewById(R.id.btn_item_cat_delete);
        }
    }
}
