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

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etName, etPhone, etAddress;
    private Button btnSave;
    private ImageView btnBack;
    private ProgressBar progressBar;

    private SessionManager sessionManager;
    private DatabaseReference databaseReference;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize session and Firebase
        sessionManager = new SessionManager(this);
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        currentUserId = sessionManager.getCurrentUserId();

        // Initialize views
        initViews();

        // Load current user data
        loadCurrentUserData();

        // Set listeners
        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveUserInfo());
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
    }

    private void loadCurrentUserData() {
        if (currentUserId != null && !currentUserId.isEmpty()) {
            showLoading(true);

            databaseReference.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    showLoading(false);

                    if (snapshot.exists()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            // Populate fields with current data
                            etName.setText(user.getName());
                            etPhone.setText(user.getPhone());
                            etAddress.setText(user.getAddress());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    showLoading(false);
                    Toast.makeText(EditProfileActivity.this, "Lỗi khi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Get from session if no userId
            User user = sessionManager.getUserFromSession();
            if (user != null) {
                etName.setText(user.getName());
                etPhone.setText(user.getPhone());
                etAddress.setText(user.getAddress());
            }
        }
    }

    private void saveUserInfo() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        // Validate input
        if (!validateInput(name, phone, address)) {
            return;
        }

        showLoading(true);

        // Update Firebase
        updateUserInFirebase(name, phone, address);
    }

    private boolean validateInput(String name, String phone, String address) {
        boolean isValid = true;

        // Validate name
        String nameError = ValidationUtils.getNameError(name);
        if (nameError != null) {
            etName.setError(nameError);
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

    private void updateUserInFirebase(String name, String phone, String address) {
        if (currentUserId == null || currentUserId.isEmpty()) {
            showLoading(false);
            Toast.makeText(this, "Không tìm thấy ID người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create update map
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("address", address);

        // Update in Firebase
        databaseReference.child(currentUserId).updateChildren(updates)
                .addOnCompleteListener(task -> {
                    showLoading(false);

                    if (task.isSuccessful()) {
                        // Update session with new data
                        updateSession(name, phone, address);

                        Toast.makeText(EditProfileActivity.this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                        finish(); // Return to previous screen
                    } else {
                        Toast.makeText(EditProfileActivity.this, "Cập nhật thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateSession(String name, String phone, String address) {
        // Get current user from session
        User currentUser = sessionManager.getUserFromSession();
        if (currentUser != null) {
            // Update user object
            currentUser.setName(name);
            currentUser.setPhone(phone);
            currentUser.setAddress(address);

            // Save updated user back to session
            boolean rememberMe = sessionManager.isRememberMe();
            sessionManager.createLoginSession(currentUser, rememberMe);
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!show);
    }
}