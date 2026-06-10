package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.firestore.DocumentSnapshot;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BannerAdminActivity extends AppCompatActivity {

    private EditText etBannerUrl;
    private Button btnAddBanner;
    private ImageView ivPreview;
    private View btnSelectImage;
    private RecyclerView rvBanners;
    private BannerAdminAdapter adapter;
    private List<Banner> bannerList;
    private BannerDAO bannerDAO;
    private Banner editingBanner = null;
    private BottomNavigationView bottomNavigationView;
    private Uri selectedImageUri;
    private boolean isUploading = false;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        ivPreview.setImageURI(selectedImageUri);
                        etBannerUrl.setText("");
                        uploadBannerToFirebase(selectedImageUri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner_admin);

        bannerDAO = new BannerDAO(this);
        initViews();
        setupBottomNavigation();
        loadBanners();
    }

    private void initViews() {
        etBannerUrl = findViewById(R.id.et_banner_url);
        btnAddBanner = findViewById(R.id.btn_add_banner);
        ivPreview = findViewById(R.id.iv_banner_preview);
        btnSelectImage = findViewById(R.id.btn_select_banner_image);
        rvBanners = findViewById(R.id.rv_banner_admin);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bannerList = new ArrayList<>();
        adapter = new BannerAdminAdapter(bannerList, new BannerAdminAdapter.OnBannerActionListener() {
            @Override
            public void onEdit(Banner banner) {
                editingBanner = banner;
                etBannerUrl.setText(banner.getImageUrl());
                selectedImageUri = null;
                Glide.with(BannerAdminActivity.this)
                        .load(GlideUtils.getGlideUrlWithUserAgent(banner.getImageUrl()))
                        .placeholder(R.drawable.ic_ball)
                        .into(ivPreview);
                btnAddBanner.setText("Cập nhật Banner");
            }

            @Override
            public void onDelete(Banner banner) {
                new AlertDialog.Builder(BannerAdminActivity.this)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc chắn muốn xóa banner này?")
                        .setPositiveButton("Xóa", (dialog, which) -> deleteBanner(banner))
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });

        rvBanners.setLayoutManager(new LinearLayoutManager(this));
        rvBanners.setAdapter(adapter);

        etBannerUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String url = s.toString().trim();
                if (!url.isEmpty()) {
                    selectedImageUri = null;
                    Glide.with(BannerAdminActivity.this)
                            .load(GlideUtils.getGlideUrlWithUserAgent(url))
                            .placeholder(R.drawable.ic_ball)
                            .into(ivPreview);
                }
            }
        });

        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        btnAddBanner.setOnClickListener(v -> {
            if (isUploading) {
                Toast.makeText(this, "Vui lòng đợi ảnh tải lên xong", Toast.LENGTH_SHORT).show();
                return;
            }

            String url = etBannerUrl.getText().toString().trim();

            if (url.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ảnh hoặc nhập link", Toast.LENGTH_SHORT).show();
                return;
            }

            if (editingBanner != null) {
                updateBanner(editingBanner, url);
            } else {
                addBanner(url);
            }
        });
    }

    private void uploadBannerToFirebase(Uri uri) {
        isUploading = true;
        btnAddBanner.setEnabled(false);
        Toast.makeText(this, "Đang tải ảnh lên Firebase...", Toast.LENGTH_SHORT).show();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("banners/" + UUID.randomUUID().toString() + ".jpg");

        storageRef.putFile(uri).addOnSuccessListener(taskSnapshot -> {
            storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                String imageUrl = downloadUri.toString();
                etBannerUrl.setText(imageUrl);
                isUploading = false;
                btnAddBanner.setEnabled(true);
                Toast.makeText(this, "Tải ảnh thành công", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            isUploading = false;
            btnAddBanner.setEnabled(true);
            Toast.makeText(this, "Lỗi tải ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            }
            if (itemId == R.id.nav_products) {
                startActivity(new Intent(this, ProductListActivity.class));
                return true;
            }
            if (itemId == R.id.nav_profile) {
                finish();
                return true;
            }
            return false;
        });
    }

    private void loadBanners() {
        bannerDAO.getAllBanners().addOnSuccessListener(queryDocumentSnapshots -> {
            bannerList.clear();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Banner banner = doc.toObject(Banner.class);
                if (banner != null) {
                    banner.setId(doc.getId());
                    bannerList.add(banner);
                }
            }
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi tải banner từ Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void addBanner(String url) {
        Banner newBanner = new Banner(null, url);
        bannerDAO.addBanner(newBanner).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Đã thêm banner vào Firebase", Toast.LENGTH_SHORT).show();
            resetForm();
            loadBanners();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi khi thêm banner: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void updateBanner(Banner banner, String newUrl) {
        banner.setImageUrl(newUrl);
        bannerDAO.updateBanner(banner).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Đã cập nhật banner trên Firebase", Toast.LENGTH_SHORT).show();
            resetForm();
            loadBanners();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi khi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void resetForm() {
        etBannerUrl.setText("");
        selectedImageUri = null;
        ivPreview.setImageResource(R.drawable.ic_ball);
        btnAddBanner.setText("Lưu Banner");
        editingBanner = null;
    }

    private void deleteBanner(Banner banner) {
        bannerDAO.deleteBanner(banner.getId()).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Đã xóa banner khỏi Firebase", Toast.LENGTH_SHORT).show();
            loadBanners();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
