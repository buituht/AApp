package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class FaqAdminActivity extends AppCompatActivity {

    private RecyclerView rvFaqAdmin;
    private FloatingActionButton fabAddFaq;
    private DatabaseHelper dbHelper;
    private List<Faq> faqList = new ArrayList<>();
    private FaqAdminAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq_admin);

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupRecyclerView();
        loadFaqs();

        findViewById(R.id.btn_back_faq).setOnClickListener(v -> finish());
        fabAddFaq.setOnClickListener(v -> startActivity(new Intent(this, AddFaqActivity.class)));
    }

    private void initViews() {
        rvFaqAdmin = findViewById(R.id.rv_faq_admin);
        fabAddFaq = findViewById(R.id.fab_add_faq);
    }

    private void setupRecyclerView() {
        adapter = new FaqAdminAdapter(this, faqList, new FaqAdminAdapter.OnFaqActionListener() {
            @Override
            public void onEdit(Faq faq) {
                Intent intent = new Intent(FaqAdminActivity.this, AddFaqActivity.class);
                intent.putExtra("faq_data", faq);
                intent.putExtra("is_edit", true);
                startActivity(intent);
            }

            @Override
            public void onDelete(Faq faq) {
                new AlertDialog.Builder(FaqAdminActivity.this)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc muốn xóa câu hỏi này?")
                        .setPositiveButton("Xóa", (dialog, which) -> deleteFaq(faq))
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });
        rvFaqAdmin.setLayoutManager(new LinearLayoutManager(this));
        rvFaqAdmin.setAdapter(adapter);
    }

    private void loadFaqs() {
        faqList.clear();
        faqList.addAll(dbHelper.getAllFaqs());
        adapter.notifyDataSetChanged();
    }

    private void deleteFaq(Faq faq) {
        dbHelper.deleteFaq(faq.getId());
        Toast.makeText(this, "Đã xóa FAQ", Toast.LENGTH_SHORT).show();
        loadFaqs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFaqs();
    }
}
