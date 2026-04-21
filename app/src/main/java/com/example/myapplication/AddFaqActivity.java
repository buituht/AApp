package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;

public class AddFaqActivity extends AppCompatActivity {

    private TextInputEditText etQuestion, etAnswer;
    private ImageView ivPreview;
    private MaterialButton btnSelectImage, btnSave, btnCancel;
    private DatabaseHelper dbHelper;
    private String selectedImageUrl = "";
    private static final int PICK_IMAGE_REQUEST = 1;
    private boolean isEdit = false;
    private Faq faqToEdit;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_faq);

        dbHelper = new DatabaseHelper(this);
        initViews();

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        if (getIntent().hasExtra("is_edit")) {
            setupEditMode();
        }

        btnSelectImage.setOnClickListener(v -> openGallery());
        btnSave.setOnClickListener(v -> saveFaq());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void initViews() {
        etQuestion = findViewById(R.id.et_faq_question);
        etAnswer = findViewById(R.id.et_faq_answer);
        ivPreview = findViewById(R.id.iv_faq_preview);
        btnSelectImage = findViewById(R.id.btn_select_faq_image);
        btnSave = findViewById(R.id.btn_save_faq);
        btnCancel = findViewById(R.id.btn_cancel_faq);
    }

    private void setupEditMode() {
        isEdit = true;
        faqToEdit = (Faq) getIntent().getSerializableExtra("faq_data");
        if (faqToEdit != null) {
            etQuestion.setText(faqToEdit.getQuestion());
            etAnswer.setText(faqToEdit.getAnswer());
            selectedImageUrl = faqToEdit.getImageUrl();
            if (selectedImageUrl != null && !selectedImageUrl.isEmpty()) {
                Glide.with(this).load(selectedImageUrl).placeholder(R.drawable.ic_ball).into(ivPreview);
            }
            btnSave.setText("CẬP NHẬT FAQ");
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            saveImageToLocal(imageUri);
        }
    }

    private void saveImageToLocal(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            String fileName = "faq_" + System.currentTimeMillis() + ".jpg";
            File file = new File(getFilesDir(), fileName);
            FileOutputStream outputStream = new FileOutputStream(file);
            
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            
            outputStream.close();
            inputStream.close();
            
            selectedImageUrl = file.getAbsolutePath();
            ivPreview.setImageURI(Uri.fromFile(file));
            Toast.makeText(this, "Đã lưu ảnh cục bộ", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi lưu ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveFaq() {
        String question = etQuestion.getText().toString().trim();
        String answer = etAnswer.getText().toString().trim();

        if (TextUtils.isEmpty(question) || TextUtils.isEmpty(answer)) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEdit && faqToEdit != null) {
            faqToEdit.setQuestion(question);
            faqToEdit.setAnswer(answer);
            faqToEdit.setImageUrl(selectedImageUrl);
            dbHelper.updateFaq(faqToEdit);
            Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Faq newFaq = new Faq(null, question, answer, selectedImageUrl);
            dbHelper.addFaq(newFaq);
            Toast.makeText(this, "Đã thêm FAQ mới", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
