package com.example.basketballshoesandroidshop.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.basketballshoesandroidshop.R;
import com.example.basketballshoesandroidshop.Utils.SessionManager;
import com.example.basketballshoesandroidshop.Utils.ValidationUtils;
import com.example.basketballshoesandroidshop.Domain.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnSignUp;
    private TextView tvSignUp;
    private CheckBox cbRememberMe;
    private ProgressBar progressBar;

    private DatabaseReference databaseReference;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo Firebase và Session
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        sessionManager = new SessionManager(this);

        // Kiểm tra đã đăng nhập chưa
        if (sessionManager.isLoggedIn()) {
            goToMainActivity();
            return;
        }

        // Ánh xạ views
        initViews();

        // Load saved email nếu có Remember Me
        loadSavedData();

        // Set listeners
        btnLogin.setOnClickListener(v -> login());
        btnSignUp.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        });
        tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        });
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvSignUp = findViewById(R.id.tvSignUp);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        progressBar = findViewById(R.id.progressBar);
    }

    private void loadSavedData() {
        // Load email từ intent (từ SignUp) hoặc từ Remember Me
        Intent intent = getIntent();
        String emailFromSignUp = intent.getStringExtra("email");

        if (emailFromSignUp != null) {
            etEmail.setText(emailFromSignUp);
        } else if (sessionManager.isRememberMe()) {
            // Load email đã lưu từ Remember Me
            User savedUser = sessionManager.getUserFromSession();
            if (savedUser != null && savedUser.getEmail() != null && !savedUser.getEmail().isEmpty()) {
                etEmail.setText(savedUser.getEmail());
                cbRememberMe.setChecked(true);
            }
        }
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        // Validate input
        if (!validateInput(email, password)) {
            return;
        }

        // Hiển thị loading
        showLoading(true);

        // Tìm user với email
        findUserByEmail(email, password);
    }

    private boolean validateInput(String email, String password) {
        boolean isValid = true;

        // Validate email
        String emailError = ValidationUtils.getEmailError(email);
        if (emailError != null) {
            etEmail.setError(emailError);
            isValid = false;
        }

        // Validate password
        if (password.isEmpty()) {
            etPassword.setError("Mật khẩu không được để trống");
            isValid = false;
        }

        return isValid;
    }

    private void findUserByEmail(String email, String password) {
        databaseReference.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        showLoading(false);

                        if (snapshot.exists()) {
                            // Tìm thấy user với email
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                User user = userSnapshot.getValue(User.class);
                                if (user != null) {
                                    // Kiểm tra password
                                    if (user.getPassword().equals(password)) {
                                        // Đăng nhập thành công
                                        loginSuccess(user);
                                        return;
                                    } else {
                                        // Sai password
                                        etPassword.setError("Mật khẩu không chính xác");
                                        Toast.makeText(LoginActivity.this, "Mật khẩu không chính xác!", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }
                            }
                        } else {
                            // Không tìm thấy user với email này
                            etEmail.setError("Email chưa được đăng ký");
                            Toast.makeText(LoginActivity.this, "Email chưa được đăng ký!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        showLoading(false);
                        Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loginSuccess(User user) {
        // Tạo session
        boolean rememberMe = cbRememberMe.isChecked();
        sessionManager.createLoginSession(user, rememberMe);

        Toast.makeText(this, "Đăng nhập thành công! Chào mừng " + user.getName(), Toast.LENGTH_SHORT).show();

        // Chuyển đến MainActivity
        goToMainActivity();
    }

    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
        btnSignUp.setEnabled(!show);
    }
}