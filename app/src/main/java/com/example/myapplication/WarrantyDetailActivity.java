package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class WarrantyDetailActivity extends AppCompatActivity {

    private WarrantyCard warrantyCard;
    private TextView tvProductName, tvOrderId, tvWarrantyId, tvActivation, tvExpiry, tvStatus, tvPeriod;
    private Button btnClaim;
    private WarrantyDAO warrantyDAO;

    private List<String> claimImages = new ArrayList<>();
    private SelectedImageAdapter imageAdapter;
    private ActivityResultLauncher<String> mGetContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warranty_detail);

        warrantyCard = (WarrantyCard) getIntent().getSerializableExtra("warranty_card");
        if (warrantyCard == null) {
            finish();
            return;
        }

        warrantyDAO = new WarrantyDAO();
        initViews();
        displayData();

        findViewById(R.id.btn_back_warranty_detail).setOnClickListener(v -> finish());
        btnClaim.setOnClickListener(v -> showClaimDialog());

        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        uploadImageToFirebase(uri);
                    }
                });
    }

    private void initViews() {
        tvProductName = findViewById(R.id.tv_detail_warranty_product);
        tvOrderId = findViewById(R.id.tv_detail_warranty_order_id);
        tvWarrantyId = findViewById(R.id.tv_detail_warranty_id);
        tvActivation = findViewById(R.id.tv_detail_warranty_activation);
        tvExpiry = findViewById(R.id.tv_detail_warranty_expiry);
        tvStatus = findViewById(R.id.tv_detail_warranty_status);
        tvPeriod = findViewById(R.id.tv_detail_warranty_period);
        btnClaim = findViewById(R.id.btn_submit_warranty_claim);
    }

    private void displayData() {
        tvProductName.setText(warrantyCard.getProductName());
        tvOrderId.setText("Mã đơn hàng: " + warrantyCard.getOrderId());
        tvWarrantyId.setText("Mã bảo hành: " + (warrantyCard.getWarrantyId() != null ? warrantyCard.getWarrantyId() : "N/A"));
        tvPeriod.setText("Thời hạn: " + warrantyCard.getWarrantyPeriod());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvActivation.setText("Ngày kích hoạt: " + sdf.format(new Date(warrantyCard.getActivationDate())));
        tvExpiry.setText("Ngày hết hạn: " + sdf.format(new Date(warrantyCard.getExpiryDate())));
        
        // Hiển thị trạng thái tiếng Việt
        String status = warrantyCard.getStatus();
        if ("Active".equals(status) || "Còn hiệu lực".equals(status)) {
            tvStatus.setText("Còn hiệu lực");
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            btnClaim.setVisibility(View.VISIBLE);
        } else {
            tvStatus.setText("Hết hạn");
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            btnClaim.setVisibility(View.GONE);
        }
    }

    private void showClaimDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_warranty_claim, null);
        builder.setView(view);

        EditText etDesc = view.findViewById(R.id.et_claim_description);
        EditText etImageUrl = view.findViewById(R.id.et_claim_image_url);
        ImageButton btnAddImageUrl = view.findViewById(R.id.btn_add_claim_image_url);
        Button btnPickImages = view.findViewById(R.id.btn_pick_claim_images);
        RecyclerView rvImages = view.findViewById(R.id.rv_claim_images);
        
        claimImages.clear();
        imageAdapter = new SelectedImageAdapter(this, claimImages, position -> {
            claimImages.remove(position);
            imageAdapter.notifyDataSetChanged();
        });
        rvImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvImages.setAdapter(imageAdapter);

        btnAddImageUrl.setOnClickListener(v -> {
            String url = etImageUrl.getText().toString().trim();
            if (!url.isEmpty()) {
                claimImages.add(url);
                imageAdapter.notifyDataSetChanged();
                etImageUrl.setText("");
            }
        });

        btnPickImages.setOnClickListener(v -> mGetContent.launch("image/*"));

        builder.setPositiveButton("Gửi yêu cầu", (dialog, which) -> {
            String desc = etDesc.getText().toString().trim();
            if (desc.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mô tả lỗi", Toast.LENGTH_SHORT).show();
                return;
            }
            submitClaim(desc, new ArrayList<>(claimImages));
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void uploadImageToFirebase(Uri uri) {
        Toast.makeText(this, "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("warranty_claims/" + UUID.randomUUID().toString());
        storageRef.putFile(uri).addOnSuccessListener(taskSnapshot -> {
            storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                claimImages.add(downloadUri.toString());
                if (imageAdapter != null) {
                    imageAdapter.notifyDataSetChanged();
                }
                Toast.makeText(this, "Tải ảnh thành công", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi tải ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void submitClaim(String description, List<String> images) {
        WarrantyClaim claim = new WarrantyClaim(
                null,
                warrantyCard.getWarrantyId(),
                warrantyCard.getProductId(),
                warrantyCard.getProductName(),
                MainActivity.currentUser != null ? MainActivity.currentUser.getEmail() : warrantyCard.getUserEmail(),
                description,
                images,
                "Chờ xử lý", // Sử dụng tiếng Việt làm trạng thái mặc định
                System.currentTimeMillis()
        );

        warrantyDAO.submitClaim(claim).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Gửi yêu cầu bảo hành thành công!", Toast.LENGTH_LONG).show();
            finish(); // Đóng màn hình sau khi gửi thành công
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi khi gửi yêu cầu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
