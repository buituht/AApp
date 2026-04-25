package com.example.myapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPhone, etAddress, etHomeAddress, etCompanyAddress, etDob, etAvatarUrl;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale;
    private MaterialButton btnSave;
    private ImageView ivAvatar;
    private BottomNavigationView bottomNavigationView;
    private UserDAO userDAO;
    private Uri selectedImageUri;
    private String oldEmail; // Để giữ email cũ làm khóa cập nhật

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    // Hiển thị xem trước ngay lập tức
                    Glide.with(this).load(selectedImageUri).circleCrop().into(ivAvatar);
                    etAvatarUrl.setText(selectedImageUri.toString());
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        userDAO = new UserDAO(this);
        initViews();
        loadUserData();
        setupBottomNavigation();

        etDob.setOnClickListener(v -> showDatePicker());
        findViewById(R.id.btn_select_avatar).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        btnSave.setOnClickListener(v -> saveProfileChanges());
    }

    private void initViews() {
        etName = findViewById(R.id.et_edit_name);
        etEmail = findViewById(R.id.et_edit_email);
        etPhone = findViewById(R.id.et_edit_phone);
        etAddress = findViewById(R.id.et_edit_address);
        etHomeAddress = findViewById(R.id.et_edit_home_address);
        etCompanyAddress = findViewById(R.id.et_edit_company_address);
        etDob = findViewById(R.id.et_edit_dob);
        etAvatarUrl = findViewById(R.id.et_edit_avatar_url);
        rgGender = findViewById(R.id.rg_edit_gender);
        rbMale = findViewById(R.id.rb_edit_male);
        rbFemale = findViewById(R.id.rb_edit_female);
        btnSave = findViewById(R.id.btn_save_profile);
        ivAvatar = findViewById(R.id.iv_edit_avatar_preview);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void loadUserData() {
        User user = MainActivity.currentUser;
        if (user != null) {
            oldEmail = user.getEmail(); // Lưu lại email gốc
            etName.setText(user.getFullName());
            etEmail.setText(user.getEmail());
            etPhone.setText(user.getPhoneNumber());
            etAddress.setText(user.getAddress());
            etHomeAddress.setText(user.getHomeAddress());
            etCompanyAddress.setText(user.getCompanyAddress());
            etDob.setText(user.getDob());
            etAvatarUrl.setText(user.getAvatarUrl());

            if ("Nam".equals(user.getGender())) rbMale.setChecked(true);
            else if ("Nữ".equals(user.getGender())) rbFemale.setChecked(true);

            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                Glide.with(this).load(user.getAvatarUrl()).placeholder(android.R.drawable.ic_menu_gallery).circleCrop().into(ivAvatar);
            }
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            etDob.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void saveProfileChanges() {
        User user = MainActivity.currentUser;
        if (user == null) return;

        String name = etName.getText().toString().trim();
        String newEmail = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String avatarUrl = etAvatarUrl.getText().toString().trim();

        if (name.isEmpty() || newEmail.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin bắt buộc", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cập nhật thông tin vào object tạm
        user.setFullName(name);
        user.setEmail(newEmail);
        user.setPhoneNumber(phone);
        user.setAddress(etAddress.getText().toString().trim());
        user.setHomeAddress(etHomeAddress.getText().toString().trim());
        user.setCompanyAddress(etCompanyAddress.getText().toString().trim());
        user.setDob(etDob.getText().toString().trim());
        user.setGender(rbMale.isChecked() ? "Nam" : "Nữ");

        // Xử lý ảnh đại diện
        if (selectedImageUri != null && avatarUrl.equals(selectedImageUri.toString())) {
            // Nếu vừa chọn ảnh từ máy, lưu vào bộ nhớ nội bộ
            String localPath = saveAvatarToInternalStorage(selectedImageUri);
            if (localPath != null) {
                user.setAvatarUrl(localPath);
            }
        } else {
            // Nếu người dùng nhập URL hoặc giữ nguyên ảnh cũ
            user.setAvatarUrl(avatarUrl);
        }

        // Cập nhật Database dùng email cũ làm định danh
        userDAO.updateUserWithOldEmail(user, oldEmail);

        MainActivity.currentUser = user;
        Toast.makeText(this, "Cập nhật hồ sơ thành công!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private String saveAvatarToInternalStorage(Uri uri) {
        try {
            File folder = new File(getFilesDir(), "avatars");
            if (!folder.exists()) folder.mkdirs();
            String fileName = "avatar_" + System.currentTimeMillis() + ".jpg";
            File file = new File(folder, fileName);
            InputStream is = getContentResolver().openInputStream(uri);
            FileOutputStream os = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) os.write(buffer, 0, length);
            os.close();
            is.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            Log.e("AvatarSave", "Error: " + e.getMessage());
            return null;
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home || item.getItemId() == R.id.nav_products) {
                finish();
                return true;
            }
            return false;
        });
    }
}
