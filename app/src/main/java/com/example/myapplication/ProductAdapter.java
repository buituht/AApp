package com.example.myapplication;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends BaseAdapter {
    private Context context;
    private List<Product> productList;
    private OnProductActionListener listener;
    private List<String> favoriteIds = new ArrayList<>();
    private boolean showAdminActions = false;
    private FavoriteDAO favoriteDAO;

    public interface OnProductActionListener {
        void onEdit(Product product);
        void onDelete(Product product);
        void onBuy(Product product);
        void onItemClick(Product product);
    }

    public ProductAdapter(Context context, List<Product> productList, OnProductActionListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
        this.favoriteDAO = new FavoriteDAO();
        loadFavoriteIds();
    }

    public void setShowAdminActions(boolean showAdminActions) {
        this.showAdminActions = showAdminActions;
        notifyDataSetChanged();
    }

    public void loadFavoriteIds() {
        if (MainActivity.isLoggedIn && MainActivity.currentUser != null) {
            favoriteDAO.getFavorites(MainActivity.currentUser.getEmail()).addOnSuccessListener(queryDocumentSnapshots -> {
                favoriteIds.clear();
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    String pid = doc.getString("productId");
                    if (pid != null) {
                        favoriteIds.add(pid);
                    }
                }
                notifyDataSetChanged();
            });
        }
    }

    @Override
    public int getCount() { return productList.size(); }
    @Override
    public Object getItem(int position) { return productList.get(position); }
    @Override
    public long getItemId(int position) { 
        try {
            return Long.parseLong(productList.get(position).getId());
        } catch (Exception e) {
            return position;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        }

        Product product = productList.get(position);

        ImageView img = convertView.findViewById(R.id.img_product);
        TextView name = convertView.findViewById(R.id.tv_product_name);
        TextView category = convertView.findViewById(R.id.tv_product_category);
        TextView price = convertView.findViewById(R.id.tv_product_price);
        TextView discountPrice = convertView.findViewById(R.id.tv_product_discount_price);
        TextView tvTag = convertView.findViewById(R.id.tv_tag);
        
        ImageButton btnFavorite = convertView.findViewById(R.id.btn_favorite);
        ImageButton btnEdit = convertView.findViewById(R.id.btn_edit);
        ImageButton btnDelete = convertView.findViewById(R.id.btn_delete);
        ImageButton btnBuy = convertView.findViewById(R.id.btn_buy);

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

        if (MainActivity.isLoggedIn && MainActivity.currentUser != null) {
            btnFavorite.setVisibility(View.VISIBLE);
            btnFavorite.setImageResource(favoriteIds.contains(product.getId()) ? 
                    R.drawable.ic_heart_solid : R.drawable.ic_heart_outline);

            if (showAdminActions) {
                btnEdit.setVisibility(View.VISIBLE);
                btnDelete.setVisibility(View.VISIBLE);
                btnBuy.setVisibility(View.GONE);
            } else {
                btnEdit.setVisibility(View.GONE);
                btnDelete.setVisibility(View.GONE);
                btnBuy.setVisibility(View.VISIBLE);
            }
        } else {
            btnFavorite.setVisibility(View.GONE);
            btnEdit.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
            btnBuy.setVisibility(View.VISIBLE);
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
        category.setText(product.getCategory() != null ? product.getCategory() : "Chưa phân loại");

        if (product.getDiscountPrice() > 0) {
            discountPrice.setVisibility(View.VISIBLE);
            discountPrice.setText(String.format("%,d VNĐ", product.getDiscountPrice()));
            price.setText(String.format("%,d VNĐ", product.getPrice()));
            price.setPaintFlags(price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            discountPrice.setVisibility(View.GONE);
            price.setText(String.format("%,d VNĐ", product.getPrice()));
            price.setPaintFlags(price.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        convertView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(product);
        });

        btnFavorite.setOnClickListener(v -> toggleFavorite(product));
        btnEdit.setOnClickListener(v -> { if (listener != null) listener.onEdit(product); });
        btnDelete.setOnClickListener(v -> { if (listener != null) listener.onDelete(product); });
        btnBuy.setOnClickListener(v -> { if (listener != null) listener.onBuy(product); });

        return convertView;
    }

    private void toggleFavorite(Product product) {
        if (!MainActivity.isLoggedIn || MainActivity.currentUser == null) {
            Toast.makeText(context, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        String userEmail = MainActivity.currentUser.getEmail();
        String productId = product.getId();

        if (favoriteIds.contains(productId)) {
            favoriteDAO.removeFavorite(userEmail, productId).addOnSuccessListener(aVoid -> {
                favoriteIds.remove(productId);
                notifyDataSetChanged();
                Toast.makeText(context, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
            });
        } else {
            favoriteDAO.addFavorite(userEmail, productId).addOnSuccessListener(aVoid -> {
                favoriteIds.add(productId);
                notifyDataSetChanged();
                Toast.makeText(context, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
