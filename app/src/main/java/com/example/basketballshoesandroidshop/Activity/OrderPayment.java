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

import com.example.basketballshoesandroidshop.Models.CreateOrder;
import com.example.basketballshoesandroidshop.R;

import org.json.JSONObject;

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
        double total = intent.getDoubleExtra("total", 0.0);
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
                                Intent intent1 = new Intent(OrderPayment.this, PaymentNotification.class);
                                intent1.putExtra("result", "Thanh toán thành công");
                                startActivity(intent1);
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
