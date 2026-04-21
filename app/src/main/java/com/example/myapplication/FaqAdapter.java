package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class FaqAdapter extends RecyclerView.Adapter<FaqAdapter.FaqViewHolder> {

    private Context context;
    private List<Faq> faqList;
    private OnFaqClickListener listener;

    public interface OnFaqClickListener {
        void onFaqClick(Faq faq);
        void onEditClick(Faq faq);
        void onDeleteClick(Faq faq);
    }

    public FaqAdapter(Context context, List<Faq> faqList, OnFaqClickListener listener) {
        this.context = context;
        this.faqList = faqList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FaqViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_news, parent, false);
        return new FaqViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FaqViewHolder holder, int position) {
        Faq faq = faqList.get(position);
        holder.tvTitle.setText(faq.getQuestion());
        holder.tvDesc.setText(faq.getAnswer());

        if (faq.getImageUrl() != null && !faq.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(faq.getImageUrl())
                    .placeholder(R.drawable.ic_ball)
                    .into(holder.ivThumb);
        } else {
            holder.ivThumb.setImageResource(R.drawable.ic_ball);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onFaqClick(faq);
        });

        // Chỉ hiện nút sửa xóa nếu là Admin và đang trong trang quản trị
        // Tuy nhiên item_news.xml không có nút sửa xóa.
        // Tôi sẽ dùng long click hoặc tạo một adapter riêng cho Admin nếu cần.
    }

    @Override
    public int getItemCount() {
        return faqList.size();
    }

    static class FaqViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb;
        TextView tvTitle, tvDesc;

        public FaqViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.iv_news_thumb);
            tvTitle = itemView.findViewById(R.id.tv_news_title);
            tvDesc = itemView.findViewById(R.id.tv_news_desc);
        }
    }
}
