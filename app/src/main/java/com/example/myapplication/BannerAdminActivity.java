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
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class BannerAdminActivity extends AppCompatActivity {

    private EditText etBannerUrl;
    private Button btnAddBanner;
    private ImageView ivPreview;
    private View btnSelectImage;
    private RecyclerView rvBanners;
    private BannerAdminAdapter adapter;
    private List<Banner> bannerList;
    private DatabaseHelper dbHelper;
    private Banner editingBanner = null;
    private BottomNavigationView bottomNavigationView;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    ivPreview.setImageURI(selectedImageUri);
                    etBannerUrl.setText("");
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner_admin);

        dbHelper = new DatabaseHelper(this);
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
            String url = etBannerUrl.getText().toString().trim();

            if (selectedImageUri == null && url.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ảnh hoặc nhập link", Toast.LENGTH_SHORT).show();
                return;
            }

            String finalImageUrl = url;
            if (selectedImageUri != null) {
                String localPath = saveImageToInternalStorage(selectedImageUri);
                if (localPath != null) {
                    finalImageUrl = localPath;
                } else {
                    Toast.makeText(this, "Lỗi khi lưu ảnh", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (editingBanner != null) {
                updateBanner(editingBanner, finalImageUrl);
            } else {
                addBanner(finalImageUrl);
            }
        });
    }

    private String saveImageToInternalStorage(Uri uri) {
        try {
            File folder = new File(getFilesDir(), "banner_images");
            if (!folder.exists()) folder.mkdirs();
            String fileName = "banner_" + System.currentTimeMillis() + ".jpg";
            File file = new File(folder, fileName);
            InputStream is = getContentResolver().openInputStream(uri);
            FileOutputStream os = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.close();
            is.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            Log.e("BannerSave", "Error: " + e.getMessage());
            return null;
        }
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
        bannerList.clear();
        bannerList.addAll(dbHelper.getAllBanners());
        adapter.notifyDataSetChanged();
    }

    private void addBanner(String url) {
        Banner newBanner = new Banner(null, url);
        dbHelper.addBanner(newBanner);
        Toast.makeText(this, "Đã thêm banner", Toast.LENGTH_SHORT).show();
        resetForm();
        loadBanners();
    }

    private void updateBanner(Banner banner, String newUrl) {
        banner.setImageUrl(newUrl);
        dbHelper.updateBanner(banner);
        Toast.makeText(this, "Đã cập nhật", Toast.LENGTH_SHORT).show();
        resetForm();
        loadBanners();
    }

    private void resetForm() {
        etBannerUrl.setText("");
        selectedImageUri = null;
        ivPreview.setImageResource(R.drawable.ic_ball);
        btnAddBanner.setText("Lưu Banner");
        editingBanner = null;
    }

    private void deleteBanner(Banner banner) {
        dbHelper.deleteBanner(banner.getId());
        Toast.makeText(this, "Đã xóa", Toast.LENGTH_SHORT).show();
        loadBanners();
    }
}
