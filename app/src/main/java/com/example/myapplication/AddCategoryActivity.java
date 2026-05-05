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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddCategoryActivity extends AppCompatActivity {

    private TextInputEditText etCategoryName, etCategoryImage;
    private MaterialButton btnSave, btnCancel;
    private View btnSelectImage;
    private ImageView ivPreview;
    private TextView tvTitle;
    private RecyclerView rvCategories;
    private BottomNavigationView bottomNavigationView;
    
    private CategoryDAO categoryDAO;
    private CategoryAdapter categoryAdapter;
    private List<Category> categoryList;
    
    private boolean isEditMode = false;
    private Category selectedCategory = null;
    private Uri selectedImageUri;
    private boolean isUploading = false;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    ivPreview.setImageURI(selectedImageUri);
                    etCategoryImage.setText("");
                    uploadCategoryImage(selectedImageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);

        categoryDAO = new CategoryDAO(this);
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
        categoryDAO.getAllCategoriesFirebase().addOnSuccessListener(queryDocumentSnapshots -> {
            categoryList.clear();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Category cat = doc.toObject(Category.class);
                if (cat != null) {
                    cat.setId(doc.getId());
                    categoryList.add(cat);
                }
            }
            categoryAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi tải danh mục: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
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

    private void uploadCategoryImage(Uri uri) {
        isUploading = true;
        btnSave.setEnabled(false);
        Toast.makeText(this, "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("categories/" + UUID.randomUUID().toString() + ".jpg");

        storageRef.putFile(uri).addOnSuccessListener(taskSnapshot -> {
            storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                String imageUrl = downloadUri.toString();
                etCategoryImage.setText(imageUrl);
                isUploading = false;
                btnSave.setEnabled(true);
                Toast.makeText(this, "Tải ảnh thành công", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            isUploading = false;
            btnSave.setEnabled(true);
            Toast.makeText(this, "Lỗi tải ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void saveCategory() {
        if (isUploading) {
            Toast.makeText(this, "Vui lòng đợi ảnh tải lên xong", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = etCategoryName.getText().toString().trim();
        String imageUrl = etCategoryImage.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEditMode && selectedCategory != null) {
            selectedCategory.setName(name);
            selectedCategory.setImageUrl(imageUrl);
            categoryDAO.updateCategory(selectedCategory).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Đã cập nhật danh mục trên Firebase!", Toast.LENGTH_SHORT).show();
                resetForm();
                loadCategories();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Category newCat = new Category(null, name, imageUrl);
            categoryDAO.addCategory(newCat).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Đã thêm danh mục mới vào Firebase!", Toast.LENGTH_SHORT).show();
                resetForm();
                loadCategories();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi thêm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
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
        categoryDAO.deleteCategory(category.getId()).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Đã xóa danh mục khỏi Firebase", Toast.LENGTH_SHORT).show();
            loadCategories();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
