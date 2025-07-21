package com.example.basketballshoesandroidshop.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUsername;
    private ImageView btnMenu;
    private DatabaseReference databaseReference;
    private SessionManager sessionManager;
    private String currentUserId;
    private ImageView ivUserAvatar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize session manager
        sessionManager = new SessionManager(this);

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            goToLogin();
            return;
        }

        // Get current user ID from session
        currentUserId = sessionManager.getCurrentUserId();

        // Ánh xạ views từ layout
        tvUsername = findViewById(R.id.tvUsername);
        ivUserAvatar = findViewById(R.id.ivUserAvatar);
        btnMenu = findViewById(R.id.btnMenu);
        ImageView btnBack = findViewById(R.id.btnBack);
        // Lấy tham chiếu đến Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Load user info
        loadUserInfo();

        // Setup menu click listener
        setupMenuListener();

        // Bắt sự kiện click cho "Xem lịch sử mua hàng"
        RelativeLayout rlPurchaseHistory = findViewById(R.id.rlPurchaseHistory);
        rlPurchaseHistory.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, OrderHistoryActivity.class);
            startActivity(intent);
        });

        setupStatusIconListeners();
    }

    private void setupMenuListener() {
        btnMenu.setOnClickListener(v -> showProfileMenu());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void showProfileMenu() {
        PopupMenu popupMenu = new PopupMenu(this, btnMenu);
        popupMenu.getMenuInflater().inflate(R.menu.profile_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.menu_view_profile) {
                    // Xem thông tin cá nhân
                    viewUserProfile();
                    return true;
                } else if (id == R.id.menu_edit_profile) {
                    // Cập nhật thông tin
                    editUserProfile();
                    return true;
                } else if (id == R.id.menu_change_password) {
                    // Đổi mật khẩu
                    changePassword();
                    return true;
                } else if (id == R.id.menu_logout) {
                    // Đăng xuất
                    showLogoutDialog();
                    return true;
                }
                return false;
            }
        });

        popupMenu.show();
    }

    private void viewUserProfile() {
        Intent intent = new Intent(ProfileActivity.this, ViewProfileActivity.class);
        startActivity(intent);
    }

    private void editUserProfile() {
        Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
        startActivity(intent);
    }

    private void changePassword() {
        Intent intent = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
        startActivity(intent);
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> logout())
                .setNegativeButton("Hủy", null)
                .show();
    }

    /*private void logout() {
        // Clear session
        sessionManager.logout();

        Toast.makeText(this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show();

        // Go to login
        goToLogin();
    }*/
    private void logout() {
        // Clear session nhưng giữ Remember Me nếu có
        if (sessionManager.isRememberMe()) {
            sessionManager.logoutKeepRemember();
        } else {
            sessionManager.logout();
        }

        Toast.makeText(this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show();

        // Go to login
        goToLogin();
    }
    private void goToLogin() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadUserInfo() {
        if (currentUserId != null && !currentUserId.isEmpty()) {
            fetchAndDisplayUsername(currentUserId);
        } else {
            // If no user ID, get from session
            User currentUser = sessionManager.getUserFromSession();
            if (currentUser != null) {
                tvUsername.setText(currentUser.getName());
            } else {
                goToLogin();
            }
        }
    }

    private void setupStatusIconListeners() {
        // Mỗi nút gọi một chỉ số riêng
        findViewById(R.id.tvPending).setOnClickListener(v -> navigateToOrderHistory(0));
        findViewById(R.id.tvToPickup).setOnClickListener(v -> navigateToOrderHistory(1));
        findViewById(R.id.tvToShip).setOnClickListener(v -> navigateToOrderHistory(2));
        findViewById(R.id.tvToRate).setOnClickListener(v -> navigateToOrderHistory(3));
    }

    private void navigateToOrderHistory(int tabIndex) {
        // In ra Logcat để kiểm tra
        Log.d("TabDebug", "Đang gửi đi chỉ số tab: " + tabIndex);

        Intent intent = new Intent(ProfileActivity.this, OrderHistoryActivity.class);
        intent.putExtra("SELECTED_TAB_INDEX", tabIndex);
        startActivity(intent);
    }

    // Phương thức để lấy và hiển thị tên người dùng
    private void fetchAndDisplayUsername(String userId) {
        // Trỏ đến nhánh của người dùng cụ thể trong "Users"
        DatabaseReference userRef = databaseReference.child("Users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Lấy giá trị của trường "name"
                    String username = snapshot.child("name").getValue(String.class);
                    String avatar = snapshot.child("avatar").getValue(String.class);
                    if (username != null) {
                        // Cập nhật TextView
                        tvUsername.setText(username);
                    }
                    loadUserAvatar(avatar);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý khi có lỗi
                Toast.makeText(ProfileActivity.this, "Lỗi khi tải tên người dùng.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void loadUserAvatar(String avatarUrl) {
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.image1)
                    .error(R.drawable.image1)
                    .circleCrop() // Làm tròn avatar
                    .into(ivUserAvatar);
        } else {
            ivUserAvatar.setImageResource(R.drawable.image1); // Default avatar
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload user info when returning to profile
        loadUserInfo();
    }
}