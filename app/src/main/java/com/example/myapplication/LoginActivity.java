package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword;
    private BottomNavigationView bottomNavigationView;
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userDAO = new UserDAO(this);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        setupBottomNavigation();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userStr = etUsername.getText().toString().trim();
                String pass = etPassword.getText().toString().trim();

                if (userStr.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Vì tìm kiếm theo username trong Firestore trả về QuerySnapshot
                userDAO.getUserByUsername(userStr).addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        User loggedInUser = doc.toObject(User.class);
                        if (loggedInUser != null && loggedInUser.getPassword() != null && loggedInUser.getPassword().equals(pass)) {
                            MainActivity.isLoggedIn = true;
                            MainActivity.isAdmin = loggedInUser.isAdmin();
                            MainActivity.currentUser = loggedInUser;
                            
                            Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                            navigateToMain();
                        } else {
                            Toast.makeText(LoginActivity.this, "Sai mật khẩu!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Tài khoản không tồn tại!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quên mật khẩu");
        builder.setMessage("Nhập email đã đăng ký để nhận lại mật khẩu:");

        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        
        int margin = (int) (20 * getResources().getDisplayMetrics().density);
        params.leftMargin = margin;
        params.rightMargin = margin;

        final EditText input = new EditText(this);
        input.setLayoutParams(params);
        input.setHint("Email của bạn");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("Gửi", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email!", Toast.LENGTH_SHORT).show();
            } else {
                findPasswordByEmail(email);
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void findPasswordByEmail(String email) {
        userDAO.getUserByEmail(email).addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                if (user != null && user.getPassword() != null) {
                    sendEmailSimulation(email, user.getPassword());
                } else {
                    Toast.makeText(this, "Không tìm thấy thông tin mật khẩu!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Email không tồn tại!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendEmailSimulation(String email, String password) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thông báo");
        builder.setMessage("Hệ thống đã gửi mật khẩu của bạn tới email: " + email + "\n\n(Giả lập: Mật khẩu của bạn là: " + password + ")");
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        android.view.Menu menu = bottomNavigationView.getMenu();
        if (menu.findItem(R.id.nav_register) != null) {
            menu.findItem(R.id.nav_register).setVisible(!MainActivity.isLoggedIn);
        }
        if (menu.findItem(R.id.nav_favorites) != null) {
            menu.findItem(R.id.nav_favorites).setVisible(MainActivity.isLoggedIn);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home || itemId == R.id.nav_products) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_register) {
                startActivity(new Intent(this, RegisterActivity.class));
                return true;
            } else if (itemId == R.id.nav_favorites) {
                startActivity(new Intent(this, FavoriteActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }
}
