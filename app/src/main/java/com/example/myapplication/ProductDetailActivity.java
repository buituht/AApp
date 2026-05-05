package com.example.myapplication;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ProductDetailActivity extends AppCompatActivity {

    private static final String TAG = "ProductDetailActivity";
    private BottomNavigationView bottomNavigationView;
    private ImageButton btnFavorite;
    private Product product;
    private DatabaseHelper dbHelper;
    private ProductDAO productDAO;
    private ReviewDAO reviewDAO;
    private RecommendationHelper recommendationHelper;

    // Gallery views
    private ImageView imgDetail;
    private RecyclerView rvProductImages;
    private ProductImageAdapter productImageAdapter;

    // Review views
    private LinearLayout layoutAddReview;
    private RatingBar rbInputRating;
    private EditText etInputComment;
    private MaterialButton btnSubmitReview;
    private NonScrollListView lvReviews;
    private List<Review> reviewList = new ArrayList<>();
    private ReviewAdapter reviewAdapter;

    // Review Image upload logic
    private LinearLayout btnPickReviewImages;
    private RecyclerView rvReviewSelectedImages;
    private SelectedImageAdapter reviewSelectedImageAdapter;
    private List<String> reviewSelectedImagesList = new ArrayList<>();
    private List<Uri> reviewSelectedUriList = new ArrayList<>();

    private final ActivityResultLauncher<Intent> reviewImagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    if (result.getData().getClipData() != null) {
                        int count = result.getData().getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri imageUri = result.getData().getClipData().getItemAt(i).getUri();
                            addReviewImageToList(imageUri);
                        }
                    } else if (result.getData().getData() != null) {
                        Uri imageUri = result.getData().getData();
                        addReviewImageToList(imageUri);
                    }
                }
            }
    );

    private void addReviewImageToList(Uri uri) {
        reviewSelectedUriList.add(uri);
        reviewSelectedImagesList.add(uri.toString());
        reviewSelectedImageAdapter.notifyItemInserted(reviewSelectedImagesList.size() - 1);
        rvReviewSelectedImages.scrollToPosition(reviewSelectedImagesList.size() - 1);
    }

    // Admin products list (Sản phẩm liên quan)
    private NonScrollListView lvAdminProducts;
    private ProductAdapter adminProductAdapter;
    private List<Product> adminProductList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_product_detail);
            dbHelper = new DatabaseHelper(this);
            productDAO = new ProductDAO(this);
            reviewDAO = new ReviewDAO(this);
            recommendationHelper = new RecommendationHelper(this);

            if (getIntent() != null) {
                product = (Product) getIntent().getSerializableExtra("product_data");
            }

            if (product == null) {
                Toast.makeText(this, "Không tìm thấy thông tin sản phẩm", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Ghi nhận lượt xem để gợi ý sau này
            recommendationHelper.recordProductView(product);

            initViews();
            initReviewAdapter(); // Khởi tạo adapter review ngay từ đầu
            initTopMenu();
            initProductContent();
            setupBottomNavigation();
            updateFavoriteIcon();
            setupReviewSection();
            setupAdminProductsSection();
            setupImageGallery();
            loadReviews();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: ", e);
            Toast.makeText(this, "Lỗi khi hiển thị chi tiết sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        imgDetail = findViewById(R.id.img_detail);
        rvProductImages = findViewById(R.id.rv_product_images);
        
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        btnFavorite = findViewById(R.id.btn_favorite_detail);
        
        layoutAddReview = findViewById(R.id.layout_add_review);
        rbInputRating = findViewById(R.id.rb_input_rating);
        etInputComment = findViewById(R.id.et_input_comment);
        btnSubmitReview = findViewById(R.id.btn_submit_review);
        lvReviews = findViewById(R.id.lv_reviews);
        
        btnPickReviewImages = findViewById(R.id.btn_pick_review_images);
        rvReviewSelectedImages = findViewById(R.id.rv_review_selected_images);

        lvAdminProducts = findViewById(R.id.lv_admin_products);
    }

    private void initReviewAdapter() {
        // Review list adapter
        reviewAdapter = new ReviewAdapter(this, reviewList, this::showFullScreenImage);
        lvReviews.setAdapter(reviewAdapter);

        // Review selection adapter
        reviewSelectedImageAdapter = new SelectedImageAdapter(this, reviewSelectedImagesList, position -> {
            reviewSelectedImagesList.remove(position);
            if (position < reviewSelectedUriList.size()) {
                reviewSelectedUriList.remove(position);
            }
            reviewSelectedImageAdapter.notifyItemRemoved(position);
            reviewSelectedImageAdapter.notifyItemRangeChanged(position, reviewSelectedImagesList.size());
        });
        rvReviewSelectedImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvReviewSelectedImages.setAdapter(reviewSelectedImageAdapter);

        if (btnPickReviewImages != null) {
            btnPickReviewImages.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                reviewImagePickerLauncher.launch(intent);
            });
        }
    }

    private void setupImageGallery() {
        if (product != null && product.getImages() != null && !product.getImages().isEmpty()) {
            productImageAdapter = new ProductImageAdapter(this, product.getImages(), this::showFullScreenImage);
            rvProductImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rvProductImages.setAdapter(productImageAdapter);
        } else {
            rvProductImages.setVisibility(View.GONE);
        }
    }

    private void showFullScreenImage(String imageUrl) {
        android.app.Dialog dialog = new android.app.Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_full_screen_image);
        
        ImageView imageView = dialog.findViewById(R.id.img_full_screen);
        ImageButton btnClose = dialog.findViewById(R.id.btn_close_full_screen);
        
        Glide.with(this)
                .load(GlideUtils.getGlideUrlWithUserAgent(imageUrl))
                .placeholder(R.drawable.ic_ball)
                .error(R.drawable.ic_ball)
                .into(imageView);
        
        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void setupAdminProductsSection() {
        adminProductAdapter = new ProductAdapter(this, adminProductList, new ProductAdapter.OnProductActionListener() {
            @Override
            public void onEdit(Product p) {}
            @Override
            public void onDelete(Product p) {}
            @Override
            public void onBuy(Product p) {
                CartActivity.cartItemList.add(p);
                Toast.makeText(ProductDetailActivity.this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onItemClick(Product p) {
                Intent intent = new Intent(ProductDetailActivity.this, ProductDetailActivity.class);
                intent.putExtra("product_data", p);
                startActivity(intent);
            }
        });
        lvAdminProducts.setAdapter(adminProductAdapter);
        loadRelatedProductsFromFirebase();
    }

    private void loadRelatedProductsFromFirebase() {
        productDAO.getAllProducts().addOnSuccessListener(queryDocumentSnapshots -> {
            adminProductList.clear();
            int count = 0;
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Product p = doc.toObject(Product.class);
                if (p != null) {
                    p.setFirebaseId(doc.getId());
                    p.setId(doc.getId());
                    if (!p.getId().equals(product.getId()) && 
                        (product.getCategory() == null || p.getCategory().equals(product.getCategory()))) {
                        adminProductList.add(p);
                        count++;
                    }
                }
                if (count >= 5) break;
            }
            
            if (count < 5) {
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    Product p = doc.toObject(Product.class);
                    if (p != null) {
                        p.setFirebaseId(doc.getId());
                        p.setId(doc.getId());
                        if (!p.getId().equals(product.getId()) && !adminProductList.contains(p)) {
                            adminProductList.add(p);
                            count++;
                        }
                    }
                    if (count >= 5) break;
                }
            }

            TextView tvTitle = findViewById(R.id.tv_title_admin_products);
            if (adminProductList.isEmpty()) {
                if (tvTitle != null) tvTitle.setVisibility(View.GONE);
                lvAdminProducts.setVisibility(View.GONE);
            } else {
                if (tvTitle != null) tvTitle.setVisibility(View.VISIBLE);
                lvAdminProducts.setVisibility(View.VISIBLE);
                adminProductAdapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error loading related products", e);
        });
    }

    private void setupReviewSection() {
        if (MainActivity.isLoggedIn && MainActivity.currentUser != null) {
            // Kiểm tra xem đã đánh giá chưa
            reviewDAO.checkIfUserReviewed(product.getId(), MainActivity.currentUser.getEmail()).addOnSuccessListener(queryDocumentSnapshots -> {
                if (queryDocumentSnapshots.isEmpty()) {
                    layoutAddReview.setVisibility(View.VISIBLE);
                    btnSubmitReview.setOnClickListener(v -> submitReview());
                } else {
                    layoutAddReview.setVisibility(View.GONE);
                }
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error checking user review status", e);
            });
        } else {
            layoutAddReview.setVisibility(View.GONE);
        }
    }

    private void submitReview() {
        String comment = etInputComment.getText().toString().trim();
        float rating = rbInputRating.getRating();

        if (comment.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập bình luận!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userName = MainActivity.currentUser.getFullName();
        if (userName == null || userName.isEmpty()) userName = MainActivity.currentUser.getUsername();

        Review review = new Review(
                product.getId(),
                MainActivity.currentUser.getEmail(),
                userName,
                rating,
                comment,
                System.currentTimeMillis(),
                new ArrayList<>() 
        );

        btnSubmitReview.setEnabled(false); // Tránh nhấn nhiều lần
        if (reviewSelectedUriList.isEmpty()) {
            saveReviewToFirestore(review);
        } else {
            uploadReviewImagesAndSubmit(review);
        }
    }

    private void uploadReviewImagesAndSubmit(Review review) {
        Toast.makeText(this, "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show();
        List<String> uploadedUrls = Collections.synchronizedList(new ArrayList<>());
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("review_images");

        AtomicInteger uploadCount = new AtomicInteger(0);
        int totalImages = reviewSelectedUriList.size();

        for (int i = 0; i < totalImages; i++) {
            Uri uri = reviewSelectedUriList.get(i);
            String fileName = "rev_" + System.currentTimeMillis() + "_" + i + ".jpg";
            StorageReference imageRef = storageRef.child(fileName);

            imageRef.putFile(uri).addOnSuccessListener(taskSnapshot -> {
                imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    uploadedUrls.add(downloadUri.toString());
                    if (uploadCount.incrementAndGet() == totalImages) {
                        review.setImages(new ArrayList<>(uploadedUrls));
                        saveReviewToFirestore(review);
                    }
                }).addOnFailureListener(e -> {
                    btnSubmitReview.setEnabled(true);
                    Toast.makeText(this, "Lỗi lấy link ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                btnSubmitReview.setEnabled(true);
                Log.e(TAG, "Error uploading review image", e);
                Toast.makeText(this, "Lỗi tải ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void saveReviewToFirestore(Review review) {
        reviewDAO.addReview(review).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Đánh giá thành công!", Toast.LENGTH_SHORT).show();
            etInputComment.setText("");
            rbInputRating.setRating(5);
            
            // Xóa dữ liệu ảnh sau khi gửi thành công
            reviewSelectedUriList.clear();
            reviewSelectedImagesList.clear();
            reviewSelectedImageAdapter.notifyDataSetChanged();
            
            layoutAddReview.setVisibility(View.GONE);
            btnSubmitReview.setEnabled(true);
            loadReviews();
        }).addOnFailureListener(e -> {
            btnSubmitReview.setEnabled(true);
            Toast.makeText(this, "Lỗi khi gửi đánh giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void loadReviews() {
        reviewDAO.getReviewsByProduct(product.getId()).addOnSuccessListener(queryDocumentSnapshots -> {
            reviewList.clear();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Review review = doc.toObject(Review.class);
                if (review != null) {
                    review.setReviewId(doc.getId());
                    reviewList.add(review);
                }
            }
            Collections.sort(reviewList, (r1, r2) -> Long.compare(r2.getTimestamp(), r1.getTimestamp()));
            reviewAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error loading reviews", e);
            Toast.makeText(this, "Lỗi tải đánh giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void initTopMenu() {
        View btnBack = findViewById(R.id.btn_back_detail);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        View btnCart = findViewById(R.id.btn_cart_detail);
        if (btnCart != null) {
            btnCart.setOnClickListener(v -> {
                startActivity(new Intent(this, CartActivity.class));
            });
        }
        
        if (btnFavorite != null) {
            if (MainActivity.isLoggedIn) {
                btnFavorite.setVisibility(View.VISIBLE);
                btnFavorite.setOnClickListener(v -> toggleFavorite());
            } else {
                btnFavorite.setVisibility(View.GONE);
            }
        }
    }

    private void updateFavoriteIcon() {
        // Favorite update logic
    }

    private void toggleFavorite() {
        if (product == null || !MainActivity.isLoggedIn || MainActivity.currentUser == null) return;
        // Toggle logic
    }

    private void initProductContent() {
        TextView tvName = findViewById(R.id.tv_detail_name);
        TextView tvPrice = findViewById(R.id.tv_detail_price);
        TextView tvDiscountPrice = findViewById(R.id.tv_detail_discount_price);
        TextView tvDesc = findViewById(R.id.tv_detail_desc);
        
        TextView tvScreen = findViewById(R.id.tv_spec_screen);
        TextView tvCpu = findViewById(R.id.tv_spec_cpu);
        TextView tvRam = findViewById(R.id.tv_spec_ram);
        TextView tvRom = findViewById(R.id.tv_spec_rom);
        TextView tvCamera = findViewById(R.id.tv_spec_camera);
        TextView tvBattery = findViewById(R.id.tv_spec_battery);
        TextView tvWarranty = findViewById(R.id.tv_spec_warranty);
        
        MaterialButton btnAddToCart = findViewById(R.id.btn_add_to_cart_detail);

        if (product != null) {
            if (imgDetail != null) {
                List<String> images = product.getImages();
                if (images != null && !images.isEmpty()) {
                    Glide.with(this)
                            .load(GlideUtils.getGlideUrlWithUserAgent(images.get(0)))
                            .placeholder(R.drawable.ic_ball)
                            .error(R.drawable.ic_ball)
                            .into(imgDetail);
                } else {
                    imgDetail.setImageResource(R.drawable.ic_ball);
                }
            }

            if (tvName != null) tvName.setText(product.getName());

            if (product.getDiscountPrice() > 0) {
                tvDiscountPrice.setVisibility(View.VISIBLE);
                tvDiscountPrice.setText(String.format("%,d VNĐ", product.getDiscountPrice()));
                tvPrice.setText(String.format("%,d VNĐ", product.getPrice()));
                tvPrice.setPaintFlags(tvPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvPrice.setTextSize(18);
            } else {
                tvDiscountPrice.setVisibility(View.GONE);
                tvPrice.setText(String.format("%,d VNĐ", product.getPrice()));
                tvPrice.setPaintFlags(tvPrice.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvPrice.setTextSize(22);
                tvPrice.setTextColor(getResources().getColor(R.color.red));
            }

            if (tvDesc != null) tvDesc.setText(product.getDescription());
            
            setSpecText(tvScreen, "Màn hình", product.getScreen());
            setSpecText(tvCpu, "Chip", product.getCpu());
            setSpecText(tvRam, "RAM", product.getRam());
            setSpecText(tvRom, "Bộ nhớ", product.getRom());
            setSpecText(tvCamera, "Camera", product.getCamera());
            setSpecText(tvBattery, "Pin", product.getBattery());
            setSpecText(tvWarranty, "Bảo hành", product.getWarranty());

            if (btnAddToCart != null) {
                btnAddToCart.setOnClickListener(v -> {
                    CartActivity.cartItemList.add(product);
                    Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    private void setSpecText(TextView textView, String label, String value) {
        if (textView == null) return;
        if (value != null && !value.trim().isEmpty()) {
            textView.setText(label + ": " + value);
        } else {
            textView.setText(label + ": --");
        }
    }

    private void setupBottomNavigation() {
        if (bottomNavigationView == null) return;

        bottomNavigationView.setOnItemSelectedListener(null);
        bottomNavigationView.setSelectedItemId(R.id.nav_products);
        
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_products) {
                Intent intent = new Intent(this, ProductListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_favorites) {
                startActivity(new Intent(this, FavoriteActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                if (MainActivity.isLoggedIn) {
                    startActivity(new Intent(this, ProfileActivity.class));
                } else {
                    startActivity(new Intent(this, LoginActivity.class));
                }
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(null);
            bottomNavigationView.setSelectedItemId(R.id.nav_products);
            setupBottomNavigation();
        }
        setupReviewSection();
        loadReviews();
        loadRelatedProductsFromFirebase();
    }
}
