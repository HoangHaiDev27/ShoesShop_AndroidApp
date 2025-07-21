package com.example.basketballshoesandroidshop.Activity;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;

import com.example.basketballshoesandroidshop.Domain.ItemsModel;
import com.example.basketballshoesandroidshop.Helper.ManagmentCart;
import com.example.basketballshoesandroidshop.Models.CreateOrder;
import com.example.basketballshoesandroidshop.R;
import com.example.basketballshoesandroidshop.Utils.SessionManager;
import com.example.basketballshoesandroidshop.Domain.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class OrderPayment extends AppCompatActivity {

        TextView txtTotal;
        Button btnThanhToan;
        Button btnLienHe;

        SessionManager sessionManager;
        User currentUser;
        String userId;
        String shippingAddress;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                EdgeToEdge.enable(this);
                setContentView(R.layout.activity_order_payment);

                sessionManager = new SessionManager(this);
                currentUser = sessionManager.getUserFromSession();
                if (currentUser != null && currentUser.getUserId() != null) {
                        userId = currentUser.getUserId();
                        shippingAddress = currentUser.getAddress();
                        if (shippingAddress == null || shippingAddress.isEmpty()) {
                                shippingAddress = "456 Đường DEF, Quận UVW, Hà Nội";
                        }
                } else {
                        userId = "user_001";
                        shippingAddress = "456 Đường DEF, Quận UVW, Hà Nội";
                }

                txtTotal = findViewById(R.id.txtTotalMoney);
                btnThanhToan = findViewById(R.id.btnThanhToan);
                btnLienHe = findViewById(R.id.btnLienHe);
                btnLienHe.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                Intent intent = new Intent(Intent.ACTION_DIAL);
                                intent.setData(android.net.Uri.parse("tel:0916450031"));
                                startActivity(intent);
                        }
                });

                // Cho phép thực hiện API trên main thread (chỉ dùng demo)
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);

                ZaloPaySDK.init(553, Environment.SANDBOX); // appID = 553 là sandbox demo

                // Nhận giá trị từ CartActivity
                Intent intent = getIntent();
                double total = intent.getDoubleExtra("total", 0.0) * 25000;
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

                                                ZaloPaySDK.getInstance().payOrder(OrderPayment.this, token,
                                                                "demozpdk://app",
                                                                new PayOrderListener() {
                                                                        @Override
                                                                        public void onPaymentSucceeded(String s,
                                                                                        String s1, String s2) {
                                                                                DatabaseReference database = FirebaseDatabase
                                                                                                .getInstance()
                                                                                                .getReference();
                                                                                String paymentMethod = "ZaloPay";

                                                                                // Lấy giỏ hàng trực tiếp từ Firebase
                                                                                DatabaseReference cartRef = database
                                                                                                .child("Cart")
                                                                                                .child(userId);
                                                                                cartRef.addListenerForSingleValueEvent(
                                                                                                new com.google.firebase.database.ValueEventListener() {
                                                                                                        @Override
                                                                                                        public void onDataChange(
                                                                                                                        @NonNull com.google.firebase.database.DataSnapshot snapshot) {
                                                                                                                List<Map<String, Object>> orderItems = new ArrayList<>();
                                                                                                                final double[] total = {
                                                                                                                                0 };
                                                                                                                List<String> itemIds = new ArrayList<>();
                                                                                                                Map<String, com.example.basketballshoesandroidshop.Domain.CartItemModel> cartMap = new HashMap<>();
                                                                                                                for (com.google.firebase.database.DataSnapshot itemSnap : snapshot
                                                                                                                                .getChildren()) {
                                                                                                                        com.example.basketballshoesandroidshop.Domain.CartItemModel item = itemSnap
                                                                                                                                        .getValue(
                                                                                                                                                        com.example.basketballshoesandroidshop.Domain.CartItemModel.class);
                                                                                                                        if (item != null) {
                                                                                                                                itemIds.add(item.getItemId());
                                                                                                                                cartMap.put(item.getItemId(),
                                                                                                                                                item);
                                                                                                                        }
                                                                                                                }
                                                                                                                // Lấy
                                                                                                                // thông
                                                                                                                // tin
                                                                                                                // ItemsModel
                                                                                                                // từ
                                                                                                                // node
                                                                                                                // Items
                                                                                                                database.child("Items")
                                                                                                                                .addListenerForSingleValueEvent(
                                                                                                                                                new com.google.firebase.database.ValueEventListener() {
                                                                                                                                                        @Override
                                                                                                                                                        public void onDataChange(
                                                                                                                                                                        @NonNull com.google.firebase.database.DataSnapshot itemsSnapshot) {
                                                                                                                                                                for (String itemId : itemIds) {
                                                                                                                                                                        com.example.basketballshoesandroidshop.Domain.CartItemModel cartItem = cartMap
                                                                                                                                                                                        .get(itemId);
                                                                                                                                                                        com.example.basketballshoesandroidshop.Domain.ItemsModel itemModel = null;
                                                                                                                                                                        for (com.google.firebase.database.DataSnapshot itemSnap : itemsSnapshot
                                                                                                                                                                                        .getChildren()) {
                                                                                                                                                                                com.example.basketballshoesandroidshop.Domain.ItemsModel temp = itemSnap
                                                                                                                                                                                                .getValue(
                                                                                                                                                                                                                com.example.basketballshoesandroidshop.Domain.ItemsModel.class);
                                                                                                                                                                                if (temp != null && itemId
                                                                                                                                                                                                .equals(temp.getId())) {
                                                                                                                                                                                        itemModel = temp;
                                                                                                                                                                                        break;
                                                                                                                                                                                }
                                                                                                                                                                        }
                                                                                                                                                                        Map<String, Object> itemMap = new HashMap<>();
                                                                                                                                                                        itemMap.put("itemId",
                                                                                                                                                                                        cartItem.getItemId());
                                                                                                                                                                        itemMap.put("title",
                                                                                                                                                                                        itemModel != null
                                                                                                                                                                                                        ? itemModel.getTitle()
                                                                                                                                                                                                        : "");
                                                                                                                                                                        itemMap.put("picUrl",
                                                                                                                                                                                        (itemModel != null
                                                                                                                                                                                                        && itemModel.getPicUrl() != null
                                                                                                                                                                                                        && !itemModel.getPicUrl()
                                                                                                                                                                                                                        .isEmpty())
                                                                                                                                                                                                                                        ? itemModel.getPicUrl()
                                                                                                                                                                                                                                                        .get(0)
                                                                                                                                                                                                                                        : "");
                                                                                                                                                                        itemMap.put("price",
                                                                                                                                                                                        cartItem.getPrice());
                                                                                                                                                                        itemMap.put("quantity",
                                                                                                                                                                                        cartItem.getQuantity());
                                                                                                                                                                        itemMap.put("size",
                                                                                                                                                                                        cartItem.getSize());
                                                                                                                                                                        itemMap.put("color",
                                                                                                                                                                                        cartItem.getColor());
                                                                                                                                                                        orderItems.add(itemMap);
                                                                                                                                                                        total[0] += cartItem
                                                                                                                                                                                        .getPrice()
                                                                                                                                                                                        * cartItem.getQuantity();
                                                                                                                                                                }
                                                                                                                                                                // Tạo
                                                                                                                                                                // OrderItem
                                                                                                                                                                // mới
                                                                                                                                                                DatabaseReference orderItemRef = database
                                                                                                                                                                                .child("OrderItem")
                                                                                                                                                                                .push();
                                                                                                                                                                String orderId = orderItemRef
                                                                                                                                                                                .getKey();
                                                                                                                                                                orderItemRef.setValue(
                                                                                                                                                                                orderItems)
                                                                                                                                                                                .addOnSuccessListener(
                                                                                                                                                                                                aVoid -> {
                                                                                                                                                                                                        // Sau
                                                                                                                                                                                                        // khi
                                                                                                                                                                                                        // lưu
                                                                                                                                                                                                        // OrderItem
                                                                                                                                                                                                        // thành
                                                                                                                                                                                                        // công
                                                                                                                                                                                                        // →
                                                                                                                                                                                                        // tạo
                                                                                                                                                                                                        // Order
                                                                                                                                                                                                        Map<String, Object> orderData = new HashMap<>();
                                                                                                                                                                                                        SimpleDateFormat isoFormat = new SimpleDateFormat(
                                                                                                                                                                                                                        "yyyy-MM-dd'T'HH:mm:ss'Z'",
                                                                                                                                                                                                                        Locale.getDefault());
                                                                                                                                                                                                        isoFormat.setTimeZone(
                                                                                                                                                                                                                        TimeZone
                                                                                                                                                                                                                                        .getTimeZone("UTC"));
                                                                                                                                                                                                        String isoDate = isoFormat
                                                                                                                                                                                                                        .format(new Date());
                                                                                                                                                                                                        orderData.put("orderDate",
                                                                                                                                                                                                                        isoDate);
                                                                                                                                                                                                        orderData.put("paidAt",
                                                                                                                                                                                                                        isoDate);
                                                                                                                                                                                                        orderData.put("isPaid",
                                                                                                                                                                                                                        true);
                                                                                                                                                                                                        orderData.put("isDelivered",
                                                                                                                                                                                                                        false);
                                                                                                                                                                                                        orderData.put("shippingAddress",
                                                                                                                                                                                                                        shippingAddress);
                                                                                                                                                                                                        orderData.put("paymentMethod",
                                                                                                                                                                                                                        paymentMethod);
                                                                                                                                                                                                        orderData.put("totalPrice",
                                                                                                                                                                                                                        total[0]);
                                                                                                                                                                                                        orderData.put("orderStatus",
                                                                                                                                                                                                                        "Chờ xác nhận");
                                                                                                                                                                                                        // đại
                                                                                                                                                                                                        // chỉ
                                                                                                                                                                                                        // mặt
                                                                                                                                                                                                        // định
                                                                                                                                                                                                        // đầu
                                                                                                                                                                                                        // tiên
                                                                                                                                                                                                        // Thủ
                                                                                                                                                                                                        // Đô
                                                                                                                                                                                                        // Hà
                                                                                                                                                                                                        // Nội
                                                                                                                                                                                                        List<Map<String, Double>> routeCoordinates = new ArrayList<>();
                                                                                                                                                                                                        Map<String, Double> start = new HashMap<>();
                                                                                                                                                                                                        start.put("lat", 21.0285);
                                                                                                                                                                                                        start.put("lng", 105.8542);
                                                                                                                                                                                                        routeCoordinates.add(
                                                                                                                                                                                                                        start);
                                                                                                                                                                                                        orderData.put("location",
                                                                                                                                                                                                                        "20,10");
                                                                                                                                                                                                        orderData.put("routeCoordinates",
                                                                                                                                                                                                                        routeCoordinates);
                                                                                                                                                                                                        // Tạo
                                                                                                                                                                                                        // trackingEvents
                                                                                                                                                                                                        // mặc
                                                                                                                                                                                                        // định
                                                                                                                                                                                                        List<Map<String, Object>> trackingEvents = new ArrayList<>();
                                                                                                                                                                                                        Map<String, Object> event = new HashMap<>();
                                                                                                                                                                                                        event.put("description",
                                                                                                                                                                                                                        "Đặt hàng thành công");
                                                                                                                                                                                                        event.put("timestamp",
                                                                                                                                                                                                                        new SimpleDateFormat(
                                                                                                                                                                                                                                        "dd 'Th'MM HH:mm",
                                                                                                                                                                                                                                        Locale.getDefault())
                                                                                                                                                                                                                                        .format(new Date()));
                                                                                                                                                                                                        event.put("isCurrent",
                                                                                                                                                                                                                        true);
                                                                                                                                                                                                        trackingEvents.add(
                                                                                                                                                                                                                        event);
                                                                                                                                                                                                        orderData.put("trackingEvents",
                                                                                                                                                                                                                        trackingEvents);
                                                                                                                                                                                                        database.child("Orders")
                                                                                                                                                                                                                        .child(userId)
                                                                                                                                                                                                                        .child(orderId)
                                                                                                                                                                                                                        .setValue(orderData)
                                                                                                                                                                                                                        .addOnSuccessListener(
                                                                                                                                                                                                                                        aVoid2 -> {
                                                                                                                                                                                                                                                // Xóa
                                                                                                                                                                                                                                                // giỏ
                                                                                                                                                                                                                                                // hàng
                                                                                                                                                                                                                                                // trong
                                                                                                                                                                                                                                                // Firebase
                                                                                                                                                                                                                                                cartRef.removeValue()
                                                                                                                                                                                                                                                                .addOnSuccessListener(
                                                                                                                                                                                                                                                                                unused -> {
                                                                                                                                                                                                                                                                                        Intent intent1 = new Intent(
                                                                                                                                                                                                                                                                                                        OrderPayment.this,
                                                                                                                                                                                                                                                                                                        PaymentNotification.class);
                                                                                                                                                                                                                                                                                        intent1.putExtra(
                                                                                                                                                                                                                                                                                                        "result",
                                                                                                                                                                                                                                                                                                        "Thanh toán thành công");
                                                                                                                                                                                                                                                                                        startActivity(
                                                                                                                                                                                                                                                                                                        intent1);
                                                                                                                                                                                                                                                                                        finish();
                                                                                                                                                                                                                                                                                });
                                                                                                                                                                                                                                        });
                                                                                                                                                                                                });
                                                                                                                                                        }

                                                                                                                                                        @Override
                                                                                                                                                        public void onCancelled(
                                                                                                                                                                        @NonNull com.google.firebase.database.DatabaseError error) {
                                                                                                                                                                Toast.makeText(OrderPayment.this,
                                                                                                                                                                                "Lỗi lấy thông tin sản phẩm từ Firebase",
                                                                                                                                                                                Toast.LENGTH_SHORT)
                                                                                                                                                                                .show();
                                                                                                                                                        }
                                                                                                                                                });
                                                                                                        }

                                                                                                        @Override
                                                                                                        public void onCancelled(
                                                                                                                        @NonNull com.google.firebase.database.DatabaseError error) {
                                                                                                                Toast.makeText(OrderPayment.this,
                                                                                                                                "Lỗi lấy giỏ hàng từ Firebase",
                                                                                                                                Toast.LENGTH_SHORT)
                                                                                                                                .show();
                                                                                                        }
                                                                                                });
                                                                        }

                                                                        @Override
                                                                        public void onPaymentCanceled(String s,
                                                                                        String s1) {
                                                                                Intent intent1 = new Intent(
                                                                                                OrderPayment.this,
                                                                                                PaymentNotification.class);
                                                                                intent1.putExtra("result",
                                                                                                "Huỷ Thanh Toán");
                                                                                startActivity(intent1);
                                                                        }

                                                                        @Override
                                                                        public void onPaymentError(
                                                                                        ZaloPayError zaloPayError,
                                                                                        String s, String s1) {
                                                                                Log.e("ZaloPayError", "Error: "
                                                                                                + zaloPayError + ", "
                                                                                                + s + ", " + s1);
                                                                                Intent intent1 = new Intent(
                                                                                                OrderPayment.this,
                                                                                                PaymentNotification.class);
                                                                                intent1.putExtra("result",
                                                                                                "Lỗi Thanh Toán");
                                                                                startActivity(intent1);
                                                                        }
                                                                });
                                        } else {
                                                Toast.makeText(OrderPayment.this, "Tạo đơn hàng thất bại!",
                                                                Toast.LENGTH_SHORT).show();
                                        }
                                } catch (Exception e) {
                                        e.printStackTrace();
                                        Toast.makeText(OrderPayment.this, "Exception: " + e.getMessage(),
                                                        Toast.LENGTH_LONG).show();
                                }
                        }
                });

        }

        @Override
        protected void onNewIntent(Intent intent) {
                super.onNewIntent(intent);
                ZaloPaySDK.getInstance().onResult(intent);
        }

        public LatLng getLocationFromAddress(Context context, String strAddress) {
                Geocoder coder = new Geocoder(context, Locale.getDefault());
                List<Address> addressList;
                try {
                        addressList = coder.getFromLocationName(strAddress, 1);
                        if (addressList == null || addressList.isEmpty()) {
                                return null;
                        }
                        Address location = addressList.get(0);
                        double lat = location.getLatitude();
                        double lng = location.getLongitude();
                        return new LatLng(lat, lng); // hoặc return new double[]{lat, lng};
                } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                }
        }
}
