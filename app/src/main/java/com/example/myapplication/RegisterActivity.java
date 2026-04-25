package com.example.myapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etUsername, etFullName, etPhone, etDob, etAddress, etHomeAddress, etCompanyAddress, etPassword;
    private RadioGroup rgGender;
    private CheckBox cbTerms;
    private Button btnRegister;
    private TextView tvLoginLink;
    private BottomNavigationView bottomNavigationView;
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        userDAO = new UserDAO(this);

        initViews();
        setupBottomNavigation();

        etDob.setOnClickListener(v -> showDatePicker());
        etDob.setFocusable(false);
        etDob.setClickable(true);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String username = etUsername.getText().toString().trim();
                String fullName = etFullName.getText().toString().trim();
                String phoneNumber = etPhone.getText().toString().trim();
                String dob = etDob.getText().toString().trim();
                String address = etAddress.getText().toString().trim();
                String homeAddress = etHomeAddress.getText().toString().trim();
                String companyAddress = etCompanyAddress.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                boolean terms = cbTerms.isChecked();

                int selectedGenderId = rgGender.getCheckedRadioButtonId();
                final String gender = (selectedGenderId != -1) ? 
                        ((RadioButton) findViewById(selectedGenderId)).getText().toString() : "";

                if (email.isEmpty() || username.isEmpty() || fullName.isEmpty() || password.isEmpty() || gender.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!terms) {
                    Toast.makeText(RegisterActivity.this, "Bạn phải đồng ý với các điều khoản!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Kiểm tra email tồn tại trong Firestore
                userDAO.getUserByEmail(email).addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Toast.makeText(RegisterActivity.this, "Email này đã được đăng ký!", Toast.LENGTH_SHORT).show();
                    } else {
                        User newUser = new User(email, username, fullName, phoneNumber, dob, gender, address, homeAddress, companyAddress, terms);
                        newUser.setPassword(password);

                        userDAO.addUser(newUser).addOnSuccessListener(aVoid -> {
                            Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            finish();
                        }).addOnFailureListener(e -> {
                            Toast.makeText(RegisterActivity.this, "Lỗi đăng ký: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }
        });

        if (tvLoginLink != null) {
            tvLoginLink.setOnClickListener(v -> finish());
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = 2000;
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String formattedDate = String.format("%02d/%02d/%d", selectedDay, (selectedMonth + 1), selectedYear);
            etDob.setText(formattedDate);
        }, year, month, day);
        
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void initViews() {
        etEmail = findViewById(R.id.et_email);
        etUsername = findViewById(R.id.et_username);
        etFullName = findViewById(R.id.et_full_name);
        etPhone = findViewById(R.id.et_phone);
        etDob = findViewById(R.id.et_dob);
        rgGender = findViewById(R.id.rg_gender);
        etAddress = findViewById(R.id.et_address);
        etHomeAddress = findViewById(R.id.et_home_address);
        etCompanyAddress = findViewById(R.id.et_company_address);
        etPassword = findViewById(R.id.et_password);
        cbTerms = findViewById(R.id.cb_terms);
        btnRegister = findViewById(R.id.btn_register);
        tvLoginLink = findViewById(R.id.tv_login_link);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_register);
        android.view.Menu menu = bottomNavigationView.getMenu();
        menu.findItem(R.id.nav_register).setVisible(!MainActivity.isLoggedIn);
        menu.findItem(R.id.nav_favorites).setVisible(MainActivity.isLoggedIn);
        
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home || itemId == R.id.nav_products) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (itemId == R.id.nav_register) {
                return true;
            } else if (itemId == R.id.nav_favorites) {
                startActivity(new Intent(this, FavoriteActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, LoginActivity.class));
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        android.view.Menu menu = bottomNavigationView.getMenu();
        menu.findItem(R.id.nav_register).setVisible(!MainActivity.isLoggedIn);
        menu.findItem(R.id.nav_favorites).setVisible(MainActivity.isLoggedIn);
    }
}
