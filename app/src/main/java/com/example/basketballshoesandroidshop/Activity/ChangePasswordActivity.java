package com.example.basketballshoesandroidshop.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.basketballshoesandroidshop.Domain.User;
import com.example.basketballshoesandroidshop.R;
import com.example.basketballshoesandroidshop.Utils.SessionManager;
import com.example.basketballshoesandroidshop.Utils.ValidationUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private Button btnChangePassword;
    private ImageView btnBack;
    private ProgressBar progressBar;

    private SessionManager sessionManager;
    private DatabaseReference databaseReference;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // Initialize session and Firebase
        sessionManager = new SessionManager(this);
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        currentUserId = sessionManager.getCurrentUserId();

        // Initialize views
        initViews();

        // Set listeners
        btnBack.setOnClickListener(v -> finish());
        btnChangePassword.setOnClickListener(v -> changePassword());
    }

    private void initViews() {
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
    }

    private void changePassword() {
        String currentPassword = etCurrentPassword.getText().toString();
        String newPassword = etNewPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        // Validate input
        if (!validateInput(currentPassword, newPassword, confirmPassword)) {
            return;
        }

        showLoading(true);

        // Verify current password and update
        verifyAndUpdatePassword(currentPassword, newPassword);
    }

    private boolean validateInput(String currentPassword, String newPassword, String confirmPassword) {
        boolean isValid = true;

        // Validate current password
        if (currentPassword.isEmpty()) {
            etCurrentPassword.setError("Mật khẩu hiện tại không được để trống");
            isValid = false;
        }

        // Validate new password
        String newPasswordError = ValidationUtils.getPasswordError(newPassword);
        if (newPasswordError != null) {
            etNewPassword.setError(newPasswordError);
            isValid = false;
        }

        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Xác nhận mật khẩu không được để trống");
            isValid = false;
        } else if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            isValid = false;
        }

        // Check if new password is same as current
        if (currentPassword.equals(newPassword)) {
            etNewPassword.setError("Mật khẩu mới phải khác mật khẩu hiện tại");
            isValid = false;
        }

        return isValid;
    }

    private void verifyAndUpdatePassword(String currentPassword, String newPassword) {
        if (currentUserId == null || currentUserId.isEmpty()) {
            showLoading(false);
            Toast.makeText(this, "Không tìm thấy ID người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current user data to verify password
        databaseReference.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        // Verify current password
                        if (user.getPassword().equals(currentPassword)) {
                            // Password correct, update to new password
                            updatePasswordInFirebase(newPassword);
                        } else {
                            // Wrong current password
                            showLoading(false);
                            etCurrentPassword.setError("Mật khẩu hiện tại không chính xác");
                            Toast.makeText(ChangePasswordActivity.this, "Mật khẩu hiện tại không chính xác!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        showLoading(false);
                        Toast.makeText(ChangePasswordActivity.this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    showLoading(false);
                    Toast.makeText(ChangePasswordActivity.this, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Toast.makeText(ChangePasswordActivity.this, "Lỗi khi kiểm tra mật khẩu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePasswordInFirebase(String newPassword) {
        // Update password in Firebase
        databaseReference.child(currentUserId).child("password").setValue(newPassword)
                .addOnCompleteListener(task -> {
                    showLoading(false);

                    if (task.isSuccessful()) {
                        // Update session with new password
                        updateSessionPassword(newPassword);

                        Toast.makeText(ChangePasswordActivity.this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();

                        // Clear input fields
                        clearFields();

                        // Optionally finish the activity
                        finish();
                    } else {
                        Toast.makeText(ChangePasswordActivity.this, "Đổi mật khẩu thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateSessionPassword(String newPassword) {
        // Get current user from session
        User currentUser = sessionManager.getUserFromSession();
        if (currentUser != null) {
            // Update password
            currentUser.setPassword(newPassword);

            // Save updated user back to session
            boolean rememberMe = sessionManager.isRememberMe();
            sessionManager.createLoginSession(currentUser, rememberMe);
        }
    }

    private void clearFields() {
        etCurrentPassword.setText("");
        etNewPassword.setText("");
        etConfirmPassword.setText("");
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnChangePassword.setEnabled(!show);
    }
}