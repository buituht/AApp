package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class CategoryAdminActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryActionListener {

    private List<Category> categoryList;
    private CategoryAdapter adapter;
    private RecyclerView rvCategories;
    private CategoryDAO categoryDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_admin);

        categoryDAO = new CategoryDAO(this);
        categoryList = new ArrayList<>();
        rvCategories = findViewById(R.id.rv_categories_admin);
        rvCategories.setLayoutManager(new LinearLayoutManager(this));

        setupAdapter();
        loadCategories();

        findViewById(R.id.btn_back_category).setOnClickListener(v -> finish());
        findViewById(R.id.fab_add_category).setOnClickListener(v -> {
            startActivity(new Intent(this, AddCategoryActivity.class));
        });
    }

    private void setupAdapter() {
        adapter = new CategoryAdapter(categoryList, this);
        rvCategories.setAdapter(adapter);
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
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi tải danh mục: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onEdit(Category category) {
        Intent intent = new Intent(CategoryAdminActivity.this, AddCategoryActivity.class);
        intent.putExtra("is_edit", true);
        intent.putExtra("category_data", category);
        startActivity(intent);
    }

    @Override
    public void onDelete(Category category) {
        new AlertDialog.Builder(CategoryAdminActivity.this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa danh mục này?")
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

    @Override
    protected void onResume() {
        super.onResume();
        loadCategories();
    }
}
