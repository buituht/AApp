package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AddCategoryActivity extends AppCompatActivity {

    private TextInputEditText etCategoryName, etCategoryImage;
    private MaterialButton btnSave, btnCancel;
    private View btnSelectImage;
    private ImageView ivPreview;
    private TextView tvTitle;
    private RecyclerView rvCategories;
    private Spinner spParentCategory;
    private BottomNavigationView bottomNavigationView;
    
    private CategoryDAO categoryDAO;
    private CategoryAdapter categoryAdapter;
    private List<Category> categoryList;
    private List<Category> parentCategoryList;
    private ArrayAdapter<String> parentAdapter;
    
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
        spParentCategory = findViewById(R.id.sp_parent_category);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        
        parentCategoryList = new ArrayList<>();
        parentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        parentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spParentCategory.setAdapter(parentAdapter);
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
        categoryDAO.getAllCategories().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Category> rawList = new ArrayList<>();
            parentCategoryList.clear();
            List<String> parentNames = new ArrayList<>();
            parentNames.add("Không có (Danh mục chính)");
            
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Category cat = doc.toObject(Category.class);
                if (cat != null) {
                    cat.setId(doc.getId());
                    rawList.add(cat);
                    
                    // Chỉ cho phép các danh mục không có parent làm parent (level 1)
                    if (cat.getParentId() == null || cat.getParentId().isEmpty()) {
                        parentCategoryList.add(cat);
                        parentNames.add(cat.getName());
                    }
                }
            }
            
            // Tổ chức danh sách hiển thị phân cấp
            organizeCategoriesHierarchically(rawList);
            
            // Cập nhật Spinner
            parentAdapter.clear();
            parentAdapter.addAll(parentNames);
            parentAdapter.notifyDataSetChanged();
            
            if (isEditMode && selectedCategory != null) {
                updateParentSpinnerSelection();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi tải danh mục: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void organizeCategoriesHierarchically(List<Category> rawList) {
        categoryList.clear();
        Map<String, List<Category>> childrenMap = new HashMap<>();
        List<Category> rootCategories = new ArrayList<>();
        Set<String> addedIds = new HashSet<>();

        // Phân loại gốc và con
        for (Category cat : rawList) {
            String pId = cat.getParentId();
            if (pId == null || pId.isEmpty()) {
                rootCategories.add(cat);
            } else {
                if (!childrenMap.containsKey(pId)) {
                    childrenMap.put(pId, new ArrayList<>());
                }
                childrenMap.get(pId).add(cat);
            }
        }

        // Sắp xếp gốc theo tên
        Collections.sort(rootCategories, (c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()));

        // Thêm vào list đệ quy
        for (Category root : rootCategories) {
            addCategoryAndChildrenToList(root, childrenMap, addedIds);
        }

        // Xử lý các mục mồ côi (nếu có)
        for (Category cat : rawList) {
            if (!addedIds.contains(cat.getId())) {
                categoryList.add(cat);
                addedIds.add(cat.getId());
            }
        }
        
        categoryAdapter.notifyDataSetChanged();
    }

    private void addCategoryAndChildrenToList(Category parent, Map<String, List<Category>> childrenMap, Set<String> addedIds) {
        if (addedIds.contains(parent.getId())) return;
        
        categoryList.add(parent);
        addedIds.add(parent.getId());
        
        List<Category> children = childrenMap.get(parent.getId());
        if (children != null) {
            Collections.sort(children, (c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()));
            for (Category child : children) {
                addCategoryAndChildrenToList(child, childrenMap, addedIds);
            }
        }
    }

    private void updateParentSpinnerSelection() {
        if (selectedCategory.getParentId() == null || selectedCategory.getParentId().isEmpty()) {
            spParentCategory.setSelection(0);
        } else {
            for (int i = 0; i < parentCategoryList.size(); i++) {
                if (parentCategoryList.get(i).getId().equals(selectedCategory.getParentId())) {
                    spParentCategory.setSelection(i + 1);
                    break;
                }
            }
        }
    }

    private void enterEditMode(Category category) {
        isEditMode = true;
        selectedCategory = category;
        tvTitle.setText("CẬP NHẬT DANH MỤC");
        btnSave.setText("CẬP NHẬT");
        btnCancel.setText("HỦY");
        
        etCategoryName.setText(category.getName());
        etCategoryImage.setText(category.getImageUrl());
        updateParentSpinnerSelection();
        
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
        spParentCategory.setSelection(0);
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
        int parentPos = spParentCategory.getSelectedItemPosition();
        String parentId = (parentPos > 0) ? parentCategoryList.get(parentPos - 1).getId() : null;

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEditMode && selectedCategory != null) {
            // Tránh chọn chính mình làm cha
            if (parentId != null && parentId.equals(selectedCategory.getId())) {
                Toast.makeText(this, "Không thể chọn chính danh mục này làm danh mục cha", Toast.LENGTH_SHORT).show();
                return;
            }
            
            selectedCategory.setName(name);
            selectedCategory.setImageUrl(imageUrl);
            selectedCategory.setParentId(parentId);
            
            categoryDAO.updateCategory(selectedCategory).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Đã cập nhật danh mục!", Toast.LENGTH_SHORT).show();
                resetForm();
                loadCategories();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Category newCat = new Category(null, name, imageUrl, parentId);
            categoryDAO.addCategory(newCat).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Đã thêm danh mục mới!", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Đã xóa danh mục", Toast.LENGTH_SHORT).show();
            loadCategories();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
