package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class ProductImageAdapter extends RecyclerView.Adapter<ProductImageAdapter.ViewHolder> {

    private Context context;
    private List<String> imageList;
    private OnImageClickListener listener;

    public interface OnImageClickListener {
        void onImageClick(String imageUrl);
    }

    public ProductImageAdapter(Context context, List<String> imageList, OnImageClickListener listener) {
        this.context = context;
        this.imageList = imageList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imageUrl = imageList.get(position);
        
        // Sử dụng GlideUtils để xử lý cả URL và Local Path
        Glide.with(context)
                .load(GlideUtils.getGlideUrlWithUserAgent(imageUrl))
                .placeholder(R.drawable.ic_ball)
                .error(R.drawable.ic_ball)
                .into(holder.imgThumbnail);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageClick(imageUrl);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageList != null ? imageList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumbnail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.img_thumbnail);
        }
    }
}
