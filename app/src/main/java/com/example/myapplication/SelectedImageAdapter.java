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

public class SelectedImageAdapter extends RecyclerView.Adapter<SelectedImageAdapter.ViewHolder> {
    private Context context;
    private List<String> images;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public SelectedImageAdapter(Context context, List<String> images, OnItemClickListener listener) {
        this.context = context;
        this.images = images;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_selected_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String url = images.get(position);
        Glide.with(context)
                .load(GlideUtils.getGlideUrlWithUserAgent(url))
                .placeholder(R.drawable.ic_ball)
                .error(R.drawable.ic_ball)
                .into(holder.imageView);
        
        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView, btnRemove;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_selected_image);
            btnRemove = itemView.findViewById(R.id.btn_remove_image);
        }
    }
}
