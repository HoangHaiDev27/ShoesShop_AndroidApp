package com.example.basketballshoesandroidshop.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.basketballshoesandroidshop.R;
import com.example.basketballshoesandroidshop.Utils.ValidationUtils;
import com.example.basketballshoesandroidshop.Domain.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SignUpActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etPhone, etAddress;
    private Button btnSignUp;
    private TextView tvLogin;
    private ProgressBar progressBar;

    private DatabaseReference databaseReference;
    private int userCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Khởi tạo Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Ánh xạ views
        initViews();

        // Lấy số lượng user hiện tại để tạo ID mới
        getUserCount();

        // Set listeners
        btnSignUp.setOnClickListener(v -> signUp());
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvLogin = findViewById(R.id.tvLogin);
        progressBar = findViewById(R.id.progressBar);
    }

    private void getUserCount() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userCounter = (int) snapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SignUpActivity.this, "Lỗi kết nối database", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signUp() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        // Validate dữ liệu
        if (!validateInput(name, email, password, phone, address)) {
            return;
        }

        // Hiển thị loading
        showLoading(true);

        // Kiểm tra email đã tồn tại chưa
        checkEmailExists(email, name, password, phone, address);
    }

    private boolean validateInput(String name, String email, String password, String phone, String address) {
        boolean isValid = true;

        // Validate name
        String nameError = ValidationUtils.getNameError(name);
        if (nameError != null) {
            etName.setError(nameError);
            isValid = false;
        }

        // Validate email
        String emailError = ValidationUtils.getEmailError(email);
        if (emailError != null) {
            etEmail.setError(emailError);
            isValid = false;
        }

        // Validate password
        String passwordError = ValidationUtils.getPasswordError(password);
        if (passwordError != null) {
            etPassword.setError(passwordError);
            isValid = false;
        }

        // Validate phone
        String phoneError = ValidationUtils.getPhoneError(phone);
        if (phoneError != null) {
            etPhone.setError(phoneError);
            isValid = false;
        }

        // Validate address
        String addressError = ValidationUtils.getAddressError(address);
        if (addressError != null) {
            etAddress.setError(addressError);
            isValid = false;
        }

        return isValid;
    }

    private void checkEmailExists(String email, String name, String password, String phone, String address) {
        databaseReference.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Email đã tồn tại
                            showLoading(false);
                            etEmail.setError("Email đã được sử dụng");
                            Toast.makeText(SignUpActivity.this, "Email đã tồn tại!", Toast.LENGTH_SHORT).show();
                        } else {
                            // Email chưa tồn tại, tiếp tục đăng ký
                            createNewUser(name, email, password, phone, address);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        showLoading(false);
                        Toast.makeText(SignUpActivity.this, "Lỗi kiểm tra email: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createNewUser(String name, String email, String password, String phone, String address) {
        // Tạo user ID mới
        userCounter++;
        String userId = String.format(Locale.getDefault(), "user_%03d", userCounter);

        // Tạo timestamp
        String createdAt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(new Date());

        // Tạo đối tượng User
        User user = new User(userId, name, email, password, phone, address, createdAt);

        // Lưu vào Firebase
        databaseReference.child(userId).setValue(user)
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(SignUpActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                        // Chuyển về màn hình đăng nhập với email đã điền sẵn
                        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                        intent.putExtra("email", email);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clear stack to prevent going back to SignUp
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(SignUpActivity.this, "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSignUp.setEnabled(!show);
    }
}