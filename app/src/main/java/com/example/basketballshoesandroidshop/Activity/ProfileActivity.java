package com.example.basketballshoesandroidshop.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.basketballshoesandroidshop.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUsername;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Ánh xạ TextView từ layout
        tvUsername = findViewById(R.id.tvUsername);

        // Lấy tham chiếu đến Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Giả sử chúng ta đang lấy thông tin cho user_001
        // Trong ứng dụng thực tế, bạn sẽ lấy ID của người dùng đã đăng nhập
        String currentUserId = "user_001";
        fetchAndDisplayUsername(currentUserId);

        // Bắt sự kiện click cho "Xem lịch sử mua hàng"
        RelativeLayout rlPurchaseHistory = findViewById(R.id.rlPurchaseHistory);
        rlPurchaseHistory.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, OrderHistoryActivity.class);
            startActivity(intent);
        });

        setupStatusIconListeners();
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
                    if (username != null) {
                        // Cập nhật TextView
                        tvUsername.setText(username);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý khi có lỗi
                Toast.makeText(ProfileActivity.this, "Lỗi khi tải tên người dùng.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
