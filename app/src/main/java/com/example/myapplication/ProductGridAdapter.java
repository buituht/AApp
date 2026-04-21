package com.example.myapplication;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class ProductGridAdapter extends BaseAdapter {
    private Context context;
    private List<Product> productList;
    private OnProductActionListener listener;

    public interface OnProductActionListener {
        void onBuy(Product product);
        void onItemClick(Product product);
    }

    public ProductGridAdapter(Context context, List<Product> productList, OnProductActionListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    @Override
    public int getCount() { return productList.size(); }
    @Override
    public Object getItem(int position) { return productList.get(position); }
    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_product_grid, parent, false);
        }

        Product product = productList.get(position);

        ImageView img = convertView.findViewById(R.id.img_product);
        TextView name = convertView.findViewById(R.id.tv_product_name);
        TextView price = convertView.findViewById(R.id.tv_product_price);
        TextView discountPrice = convertView.findViewById(R.id.tv_product_discount_price);
        TextView tvTag = convertView.findViewById(R.id.tv_tag);
        MaterialButton btnBuy = convertView.findViewById(R.id.btn_buy_grid);

        // Hiển thị Tag HOT/NEW
        if (tvTag != null) {
            if (product.isHotDiscount()) {
                tvTag.setVisibility(View.VISIBLE);
                tvTag.setText("HOT");
                tvTag.setBackgroundResource(android.R.color.holo_red_dark);
            } else if (product.isNewArrival()) {
                tvTag.setVisibility(View.VISIBLE);
                tvTag.setText("NEW");
                tvTag.setBackgroundResource(android.R.color.holo_blue_dark);
            } else {
                tvTag.setVisibility(View.GONE);
            }
        }

        if (product.getImages() != null && !product.getImages().isEmpty()) {
            Glide.with(context)
                    .load(GlideUtils.getGlideUrlWithUserAgent(product.getImages().get(0)))
                    .placeholder(R.drawable.ic_ball)
                    .error(R.drawable.ic_ball)
                    .into(img);
        } else {
            img.setImageResource(R.drawable.ic_ball);
        }
        
        name.setText(product.getName());

        if (product.getDiscountPrice() > 0) {
            price.setText(String.format("%,d VNĐ", product.getDiscountPrice()));
            discountPrice.setVisibility(View.VISIBLE);
            discountPrice.setText(String.format("%,d VNĐ", product.getPrice()));
            discountPrice.setPaintFlags(discountPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            price.setText(String.format("%,d VNĐ", product.getPrice()));
            discountPrice.setVisibility(View.GONE);
        }

        convertView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(product);
        });

        btnBuy.setOnClickListener(v -> {
            if (listener != null) listener.onBuy(product);
        });

        return convertView;
    }
}
