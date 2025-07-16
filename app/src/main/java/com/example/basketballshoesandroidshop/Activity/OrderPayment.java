package com.example.basketballshoesandroidshop.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.basketballshoesandroidshop.Domain.ItemsModel;
import com.example.basketballshoesandroidshop.Helper.ManagmentCart;
import com.example.basketballshoesandroidshop.Models.CreateOrder;
import com.example.basketballshoesandroidshop.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class OrderPayment extends AppCompatActivity {

    TextView txtTotal;
    Button btnThanhToan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_payment);

        txtTotal = findViewById(R.id.txtTotalMoney);
        btnThanhToan = findViewById(R.id.btnThanhToan);

        // Cho phép thực hiện API trên main thread (chỉ dùng demo)
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        ZaloPaySDK.init(553, Environment.SANDBOX); // appID = 553 là sandbox demo

        // Nhận giá trị từ CartActivity
        Intent intent = getIntent();
        double total = intent.getDoubleExtra("total", 0.0)*25000;
        String totalString = String.format("%.0f", total);
        txtTotal.setText("$" + totalString);

        btnThanhToan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateOrder orderApi = new CreateOrder();
                try {
                    JSONObject data = orderApi.createOrder(totalString);
                    Log.d("OrderPayment", "ZaloPay Order Response: " + data.toString());

                    String code = data.getString("returncode");
                    if (code.equals("1")) {
                        String token = data.getString("zptranstoken");

                        ZaloPaySDK.getInstance().payOrder(OrderPayment.this, token, "demozpdk://app", new PayOrderListener() {
                            @Override
                            public void onPaymentSucceeded(String s, String s1, String s2) {
                                DatabaseReference database = FirebaseDatabase.getInstance().getReference();
                                String userId = "user_001"; // hoặc lấy từ user đã login
                                String shippingAddress = "456 Đường DEF, Quận UVW, Hà Nội";
                                String paymentMethod = "ZaloPay";

                                ManagmentCart managmentCart = new ManagmentCart(OrderPayment.this);
                                ArrayList<ItemsModel> cartList = managmentCart.getListCart();
                                double total = managmentCart.getTotalFee();

                                // Tạo OrderItem mới
                                DatabaseReference orderItemRef = database.child("OrderItem").push();
                                String orderId = orderItemRef.getKey();

                                List<Map<String, Object>> orderItems = new ArrayList<>();
                                for (ItemsModel item : cartList) {
                                    Map<String, Object> itemMap = new HashMap<>();
                                    itemMap.put("itemId", item.getId());
                                    itemMap.put("title", item.getTitle());
                                    itemMap.put("price", item.getPrice());
                                    itemMap.put("quantity", item.getNumberInCart());
                                    itemMap.put("size", item.getSize() instanceof List ? ((List<?>) item.getSize()).get(0) : item.getSize()); // lấy size đầu tiên nếu là list
                                    itemMap.put("picUrl", item.getPicUrl().get(0));
                                    orderItems.add(itemMap);
                                }

                                // Lưu OrderItem
                                orderItemRef.setValue(orderItems).addOnSuccessListener(aVoid -> {
                                    // Sau khi lưu OrderItem thành công → tạo Order
                                    Map<String, Object> orderData = new HashMap<>();
                                    orderData.put("orderDate", ServerValue.TIMESTAMP);
                                    orderData.put("isPaid", true);
                                    orderData.put("isDelivered", false);
                                    orderData.put("shippingAddress", shippingAddress);
                                    orderData.put("paymentMethod", paymentMethod);
                                    orderData.put("totalPrice", total);
                                    orderData.put("orderStatus", "Chờ xác nhận");

                                    // Tạo trackingEvents mặc định
                                    List<Map<String, Object>> trackingEvents = new ArrayList<>();
                                    Map<String, Object> event = new HashMap<>();
                                    event.put("description", "Đặt hàng thành công");
                                    event.put("timestamp", new SimpleDateFormat("dd 'Th'MM HH:mm", Locale.getDefault()).format(new Date()));
                                    event.put("isCurrent", true);
                                    trackingEvents.add(event);
                                    orderData.put("trackingEvents", trackingEvents);

                                    database.child("Orders").child(userId).child(orderId)
                                            .setValue(orderData)
                                            .addOnSuccessListener(aVoid2 -> {
                                                // Xóa giỏ hàng trong Firebase
                                                database.child("Cart").child(userId).removeValue()
                                                        .addOnSuccessListener(unused -> {
                                                            Intent intent1 = new Intent(OrderPayment.this, PaymentNotification.class);
                                                            intent1.putExtra("result", "Thanh toán thành công");
                                                            startActivity(intent1);
                                                            finish();
                                                        });
                                            });
                                });
                            }


                            @Override
                            public void onPaymentCanceled(String s, String s1) {
                                Intent intent1 = new Intent(OrderPayment.this, PaymentNotification.class);
                                intent1.putExtra("result", "Huỷ Thanh Toán");
                                startActivity(intent1);
                            }

                            @Override
                            public void onPaymentError(ZaloPayError zaloPayError, String s, String s1) {
                                Log.e("ZaloPayError", "Error: " + zaloPayError + ", " + s + ", " + s1);
                                Intent intent1 = new Intent(OrderPayment.this, PaymentNotification.class);
                                intent1.putExtra("result", "Lỗi Thanh Toán");
                                startActivity(intent1);
                            }
                        });
                    } else {
                        Toast.makeText(OrderPayment.this, "Tạo đơn hàng thất bại!", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(OrderPayment.this, "Exception: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent);
    }
}
