package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class FaqAdminActivity extends AppCompatActivity {

    private RecyclerView rvFaqAdmin;
    private FloatingActionButton fabAddFaq;
    private FaqDAO faqDAO;
    private List<Faq> faqList = new ArrayList<>();
    private FaqAdminAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq_admin);

        faqDAO = new FaqDAO(this);
        initViews();
        setupRecyclerView();
        loadFaqsFromFirebase();

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

    private void loadFaqsFromFirebase() {
        faqDAO.getAllFaqs().addOnSuccessListener(queryDocumentSnapshots -> {
            faqList.clear();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Faq faq = doc.toObject(Faq.class);
                if (faq != null) {
                    faq.setId(doc.getId());
                    faqList.add(faq);
                }
            }
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Log.e("FaqAdminActivity", "Error loading FAQs", e);
            Toast.makeText(this, "Lỗi khi tải FAQ từ Firebase", Toast.LENGTH_SHORT).show();
        });
    }

    private void deleteFaq(Faq faq) {
        faqDAO.deleteFaq(faq.getId()).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Đã xóa FAQ", Toast.LENGTH_SHORT).show();
            loadFaqsFromFirebase();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi khi xóa FAQ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFaqsFromFirebase();
    }
}
