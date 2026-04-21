package com.example.myapplication;

import android.content.Context;
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

public class FaqAdminAdapter extends RecyclerView.Adapter<FaqAdminAdapter.FaqAdminViewHolder> {

    private Context context;
    private List<Faq> faqList;
    private OnFaqActionListener listener;

    public interface OnFaqActionListener {
        void onEdit(Faq faq);
        void onDelete(Faq faq);
    }

    public FaqAdminAdapter(Context context, List<Faq> faqList, OnFaqActionListener listener) {
        this.context = context;
        this.faqList = faqList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FaqAdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_faq_admin, parent, false);
        return new FaqAdminViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FaqAdminViewHolder holder, int position) {
        Faq faq = faqList.get(position);
        holder.tvQuestion.setText(faq.getQuestion());
        holder.tvAnswer.setText(faq.getAnswer());

        if (faq.getImageUrl() != null && !faq.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(GlideUtils.getGlideUrlWithUserAgent(faq.getImageUrl()))
                    .placeholder(R.drawable.ic_ball)
                    .into(holder.ivThumb);
        } else {
            holder.ivThumb.setImageResource(R.drawable.ic_ball);
        }

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(faq);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(faq);
        });
    }

    @Override
    public int getItemCount() {
        return faqList.size();
    }

    static class FaqAdminViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb;
        TextView tvQuestion, tvAnswer;
        ImageButton btnEdit, btnDelete;

        public FaqAdminViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.iv_faq_thumb);
            tvQuestion = itemView.findViewById(R.id.tv_faq_question);
            tvAnswer = itemView.findViewById(R.id.tv_faq_answer);
            btnEdit = itemView.findViewById(R.id.btn_edit_faq);
            btnDelete = itemView.findViewById(R.id.btn_delete_faq);
        }
    }
}
