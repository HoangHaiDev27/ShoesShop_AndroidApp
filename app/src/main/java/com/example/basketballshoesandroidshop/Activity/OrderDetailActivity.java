package com.example.basketballshoesandroidshop.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.basketballshoesandroidshop.Adapter.OrderDetailAdapter;
import com.example.basketballshoesandroidshop.Domain.OrderItemModel;
import com.example.basketballshoesandroidshop.Domain.OrderModel;
import com.example.basketballshoesandroidshop.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView tvOrderStatusHeader, tvDeliveryStatus, tvDeliveryDate;
    private TextView tvCustomerName, tvShippingAddress, tvStoreName, tvTotalPrice,tvTrackingInfo;
    private RecyclerView rvOrderItems;

    private String userId;
    private String orderId;

    private LinearLayout llStatusHeader;
    private ImageView ivStatusIcon;
    private OrderDetailAdapter orderDetailAdapter;
    private Button btnBuyAgain, btnRate;
    private List<OrderItemModel> itemList;

    private DatabaseReference databaseReference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        Toolbar toolbar = findViewById(R.id.toolbarDetail);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

         userId = getIntent().getStringExtra("USER_ID");
         orderId = getIntent().getStringExtra("ORDER_ID");

        initViews();

        databaseReference = FirebaseDatabase.getInstance().getReference();

        if (userId != null && orderId != null) {
            // Bước 1: Lấy thông tin chính của đơn hàng
            fetchOrderDetails(userId, orderId);
            // Bước 2: Lấy danh sách sản phẩm cho đơn hàng đó
            fetchItemsForOrder(orderId);
        }

        setupActionListeners();
    }

    private void setupActionListeners() {
        TextView tvContactShop = findViewById(R.id.tvContactShop);

        tvContactShop.setOnClickListener(v -> {
            // Gọi phương thức để gửi email
            sendEmailToShop();
        });
    }

    private void sendEmailToShop() {
        // Lấy thông tin cần thiết. Tốt nhất là lấy từ dữ liệu đơn hàng.
        String shopEmail = "binvro6969@gmail.com"; // Email của cửa hàng
        String subject = "Hỗ trợ về đơn hàng #" + orderId; // orderId đã được lấy từ intent
        String body = "Xin chào, tôi cần hỗ trợ về đơn hàng có mã " + orderId + ". Vấn đề của tôi là: ";

        // Tạo một Intent để gửi email
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // Chỉ các ứng dụng email mới nên xử lý intent này
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{shopEmail}); // Mảng các địa chỉ email người nhận
        intent.putExtra(Intent.EXTRA_SUBJECT, subject); // Tiêu đề email
        intent.putExtra(Intent.EXTRA_TEXT, body); // Nội dung email


        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "Không tìm thấy ứng dụng email.", Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        tvOrderStatusHeader = findViewById(R.id.tvOrderStatusHeader);

        tvDeliveryStatus = findViewById(R.id.tvDeliveryStatus);
        tvTrackingInfo = findViewById(R.id.tvTrackingInfo);
        tvDeliveryDate = findViewById(R.id.tvDeliveryDate);
        tvDeliveryDate = findViewById(R.id.tvDeliveryDate);
        tvCustomerName = findViewById(R.id.tvCustomerName);
        tvShippingAddress = findViewById(R.id.tvShippingAddress);
        tvStoreName = findViewById(R.id.tvStoreName);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnBuyAgain = findViewById(R.id.btnBuyAgain);
        btnRate = findViewById(R.id.btnRate);

        // Khởi tạo RecyclerView
        rvOrderItems = findViewById(R.id.rvOrderItems);
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo danh sách và adapter
        itemList = new ArrayList<>();
        orderDetailAdapter = new OrderDetailAdapter(itemList, this);
        rvOrderItems.setAdapter(orderDetailAdapter);
        llStatusHeader = findViewById(R.id.llStatusHeader);
        ivStatusIcon = findViewById(R.id.ivStatusIcon);

        LinearLayout llShippingInfo = findViewById(R.id.llShippingInfo);
        llShippingInfo.setOnClickListener(v -> {
            Intent intent = new Intent(OrderDetailActivity.this, TrackingActivity.class);
            intent.putExtra("USER_ID", userId); // userId đã lấy từ intent trước đó
            intent.putExtra("ORDER_ID", orderId); // orderId đã lấy từ intent trước đó
            startActivity(intent);
        });


    }

    private void fetchOrderDetails(String userId, String orderId) {
        DatabaseReference orderRef = databaseReference.child("Orders").child(userId).child(orderId);

        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                OrderModel order = snapshot.getValue(OrderModel.class);
                if (order != null) {
                    bindGeneralOrderInfo(order);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi
            }
        });
    }

    // Phương thức mới để lấy các item từ nhánh "OrderItem"
    private void fetchItemsForOrder(String orderId) {
        DatabaseReference itemsRef = databaseReference.child("OrderItem").child(orderId);

        itemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Xóa dữ liệu cũ trước khi thêm mới
                itemList.clear();

                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    OrderItemModel item = itemSnapshot.getValue(OrderItemModel.class);
                    if (item != null) {
                        itemList.add(item);
                    }
                }

                // Cập nhật lại RecyclerView
                orderDetailAdapter.notifyDataSetChanged();

                // Cập nhật thông tin cửa hàng từ item đầu tiên (nếu cần)
                if (!itemList.isEmpty()) {
                    // tvStoreName.setText(itemList.get(0).getStoreName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("OrderDetailActivity", "Lỗi khi lấy item: ", databaseError.toException());
            }
        });
    }

    // Đổi tên phương thức cũ và chỉ bind thông tin chung
    private void bindGeneralOrderInfo(OrderModel order) {
        // --- BIND CÁC THÔNG TIN CHUNG KHÁC ---
        tvDeliveryDate.setText(order.getDeliveredAt());
        tvShippingAddress.setText(order.getShippingAddress());
        tvTotalPrice.setText(String.format("₫%,.0f", order.getTotalPrice()));

        // --- BIND THÔNG TIN TRẠNG THÁI LINH HOẠT ---
        String status = order.getOrderStatus();

        int statusColor;
        int statusBgColor;
        int statusIconRes; // Biến để lưu ID của icon

        switch (status) {
            case "Đã giao":
            case "Hoàn thành":
                tvOrderStatusHeader.setText("Đơn hàng đã hoàn thành");
                tvDeliveryStatus.setText("Giao hàng thành công");
                statusColor = ContextCompat.getColor(this, R.color.status_green_text);
                statusBgColor = ContextCompat.getColor(this, R.color.status_green_bg);
                statusIconRes = R.drawable.ic_check_circle;
                break;

            case "Chờ xác nhận":
                tvOrderStatusHeader.setText("Chờ xác nhận");
                tvDeliveryStatus.setText("Đặt hàng thành công");
                statusColor = ContextCompat.getColor(this, R.color.status_blue_text);
                statusBgColor = ContextCompat.getColor(this, R.color.status_blue_bg);
                statusIconRes = R.drawable.ic_pending_confirmation;
                break;

            case "Đang giao":
                tvOrderStatusHeader.setText("Đơn hàng đang được giao");
                tvDeliveryStatus.setText("Đang trên đường giao đến bạn");
                statusColor = ContextCompat.getColor(this, R.color.primary_orange);
                // Giả sử dùng chung màu nền với trạng thái chờ
                statusBgColor = ContextCompat.getColor(this, R.color.status_blue_bg);
                statusIconRes = R.drawable.ic_local_shipping;
                break;

            case "Đã hủy":
                tvOrderStatusHeader.setText("Đơn hàng đã hủy");
                tvDeliveryStatus.setText("Đã hủy bởi bạn hoặc người bán");
                statusColor = ContextCompat.getColor(this, R.color.status_red_text);
                statusBgColor = ContextCompat.getColor(this, R.color.status_red_bg);
                statusIconRes = R.drawable.ic_cancel;
                break;

            default:
                tvOrderStatusHeader.setText(status);
                tvDeliveryStatus.setText("Cập nhật cuối");
                statusColor = ContextCompat.getColor(this, android.R.color.darker_gray);
                statusBgColor = ContextCompat.getColor(this, R.color.light_gray_background);
                statusIconRes = R.drawable.ic_help_outline;
                break;
        }

        // Áp dụng các thay đổi
        llStatusHeader.setBackgroundColor(statusBgColor);
        ivStatusIcon.setImageResource(statusIconRes);
        ivStatusIcon.setColorFilter(statusColor, android.graphics.PorterDuff.Mode.SRC_IN);
        tvOrderStatusHeader.setTextColor(statusColor);
        tvDeliveryStatus.setTextColor(statusColor);
        tvTrackingInfo.setText("SPXVN055731637836");
    }
}
