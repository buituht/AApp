package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AddCategoryActivity extends AppCompatActivity {

    private TextInputEditText etCategoryName, etCategoryImage;
    private MaterialButton btnSave, btnCancel;
    private View btnSelectImage;
    private ImageView ivPreview;
    private TextView tvTitle;
    private RecyclerView rvCategories;
    private BottomNavigationView bottomNavigationView;
    
    private DatabaseHelper dbHelper;
    private CategoryAdapter categoryAdapter;
    private List<Category> categoryList;
    
    private boolean isEditMode = false;
    private Category selectedCategory = null;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    ivPreview.setImageURI(selectedImageUri);
                    etCategoryImage.setText("");
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupRecyclerView();
        setupImagePreview();
        setupBottomNavigation();
        loadCategories();

        btnCancel.setOnClickListener(v -> {
            if (isEditMode) {
                resetForm();
            } else {
                finish();
            }
        });
        
        btnSave.setOnClickListener(v -> saveCategory());
        
        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });
    }

    private void initViews() {
        etCategoryName = findViewById(R.id.et_category_name);
        etCategoryImage = findViewById(R.id.et_category_image);
        btnSave = findViewById(R.id.btn_save_category);
        btnCancel = findViewById(R.id.btn_cancel_category);
        btnSelectImage = findViewById(R.id.btn_select_category_image);
        ivPreview = findViewById(R.id.iv_category_preview);
        tvTitle = findViewById(R.id.tv_add_category_title);
        rvCategories = findViewById(R.id.rv_categories_manage);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
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

    private void setupRecyclerView() {
        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(categoryList, new CategoryAdapter.OnCategoryActionListener() {
            @Override
            public void onEdit(Category category) {
                enterEditMode(category);
            }

            @Override
            public void onDelete(Category category) {
                showDeleteConfirmation(category);
            }
        });
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        rvCategories.setAdapter(categoryAdapter);
    }

    private void setupImagePreview() {
        etCategoryImage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String url = s.toString().trim();
                if (!url.isEmpty()) {
                    selectedImageUri = null;
                    Glide.with(AddCategoryActivity.this)
                            .load(GlideUtils.getGlideUrlWithUserAgent(url))
                            .placeholder(R.drawable.ic_ball)
                            .error(R.drawable.ic_ball)
                            .into(ivPreview);
                } else if (selectedImageUri == null) {
                    ivPreview.setImageResource(R.drawable.ic_ball);
                }
            }
        });
    }

    private void loadCategories() {
        categoryList.clear();
        categoryList.addAll(dbHelper.getAllCategories());
        categoryAdapter.notifyDataSetChanged();
    }

    private void enterEditMode(Category category) {
        isEditMode = true;
        selectedCategory = category;
        tvTitle.setText("CẬP NHẬT DANH MỤC");
        btnSave.setText("CẬP NHẬT");
        btnCancel.setText("HỦY");
        
        etCategoryName.setText(category.getName());
        etCategoryImage.setText(category.getImageUrl());
        selectedImageUri = null;
        if (!TextUtils.isEmpty(category.getImageUrl())) {
            Glide.with(this)
                    .load(GlideUtils.getGlideUrlWithUserAgent(category.getImageUrl()))
                    .placeholder(R.drawable.ic_ball)
                    .into(ivPreview);
        }
    }

    private void resetForm() {
        isEditMode = false;
        selectedCategory = null;
        selectedImageUri = null;
        tvTitle.setText("THÊM DANH MỤC MỚI");
        btnSave.setText("LƯU");
        btnCancel.setText("HỦY BỎ");
        
        etCategoryName.setText("");
        etCategoryImage.setText("");
        ivPreview.setImageResource(R.drawable.ic_ball);
    }

    private String saveImageToInternalStorage(Uri uri) {
        try {
            File folder = new File(getFilesDir(), "category_images");
            if (!folder.exists()) folder.mkdirs();
            String fileName = "cat_" + System.currentTimeMillis() + ".jpg";
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
            Log.e("CategorySave", "Error: " + e.getMessage());
            return null;
        }
    }

    private void saveCategory() {
        String name = etCategoryName.getText().toString().trim();
        String imageUrl = etCategoryImage.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        String finalImageUrl = imageUrl;
        if (selectedImageUri != null) {
            String localPath = saveImageToInternalStorage(selectedImageUri);
            if (localPath != null) {
                finalImageUrl = localPath;
            } else {
                Toast.makeText(this, "Lỗi khi lưu ảnh", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (isEditMode && selectedCategory != null) {
            selectedCategory.setName(name);
            selectedCategory.setImageUrl(finalImageUrl);
            dbHelper.updateCategory(selectedCategory);
            Toast.makeText(this, "Đã cập nhật danh mục!", Toast.LENGTH_SHORT).show();
            resetForm();
            loadCategories();
        } else {
            Category newCat = new Category();
            newCat.setName(name);
            newCat.setImageUrl(finalImageUrl);
            dbHelper.addCategory(newCat);
            Toast.makeText(this, "Đã thêm danh mục mới!", Toast.LENGTH_SHORT).show();
            resetForm();
            loadCategories();
        }
    }

    private void showDeleteConfirmation(Category category) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa danh mục '" + category.getName() + "'?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteCategory(category))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteCategory(Category category) {
        dbHelper.deleteCategory(category.getId());
        Toast.makeText(this, "Đã xóa danh mục", Toast.LENGTH_SHORT).show();
        loadCategories();
    }
}
