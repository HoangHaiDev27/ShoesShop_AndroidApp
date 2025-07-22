package com.example.basketballshoesandroidshop.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.basketballshoesandroidshop.Domain.User;
import com.example.basketballshoesandroidshop.R;
import com.example.basketballshoesandroidshop.Utils.SessionManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ViewProfileActivity extends AppCompatActivity {

    private TextView tvUserId, tvName, tvEmail, tvPhone, tvAddress, tvCreatedAt;
    private ImageView btnBack;
    private SessionManager sessionManager;
    private DatabaseReference databaseReference;
    private ImageView ivProfileAvatar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        // Initialize session and Firebase
        sessionManager = new SessionManager(this);
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize views
        initViews();

        // Load user data
        loadUserData();

        // Setup back button
        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        tvUserId = findViewById(R.id.tvUserId);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvAddress = findViewById(R.id.tvAddress);
        ivProfileAvatar = findViewById(R.id.ivProfileAvatar);
        tvCreatedAt = findViewById(R.id.tvCreatedAt);
        btnBack = findViewById(R.id.btnBack);
    }

    private void loadUserData() {
        String userId = sessionManager.getCurrentUserId();

        if (userId != null && !userId.isEmpty()) {
            databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            displayUserData(user);
                        }
                    } else {
                        Toast.makeText(ViewProfileActivity.this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ViewProfileActivity.this, "Lỗi khi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Get from session if no userId
            User user = sessionManager.getUserFromSession();
            if (user != null) {
                displayUserData(user);
            }
        }
    }

    private void displayUserData(User user) {
        tvUserId.setText(user.getUserId() != null ? user.getUserId() : "N/A");
        tvName.setText(user.getName() != null ? user.getName() : "N/A");
        tvEmail.setText(user.getEmail() != null ? user.getEmail() : "N/A");
        tvPhone.setText(user.getPhone() != null ? user.getPhone() : "N/A");
        tvAddress.setText(user.getAddress() != null ? user.getAddress() : "N/A");
        tvCreatedAt.setText(user.getCreatedAt() != null ? formatDate(user.getCreatedAt()) : "N/A");
        loadUserAvatar(user.getAvatar());
    }
    private void loadUserAvatar(String avatarUrl) {
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.image1)
                    .error(R.drawable.image1)
                    .circleCrop() // Làm tròn avatar
                    .into(ivProfileAvatar);
        } else {
            ivProfileAvatar.setImageResource(R.drawable.image1); // Default avatar
        }
    }
    private String formatDate(String dateString) {
        try {
            // Convert ISO date to readable format
            if (dateString.contains("T")) {
                String[] parts = dateString.split("T");
                return parts[0]; // Return date part only
            }
            return dateString;
        } catch (Exception e) {
            return dateString;
        }
    }
}