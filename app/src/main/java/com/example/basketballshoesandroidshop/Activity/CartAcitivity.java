package com.example.basketballshoesandroidshop.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.basketballshoesandroidshop.Adapter.CartAdapter;
import com.example.basketballshoesandroidshop.Domain.CartItemModel;
import com.example.basketballshoesandroidshop.Domain.ItemsModel;
import com.example.basketballshoesandroidshop.Helper.ChangeNumberItemsListener;
import com.example.basketballshoesandroidshop.Helper.ManagmentCart;
import com.example.basketballshoesandroidshop.R;
import com.example.basketballshoesandroidshop.Repository.MainRepository;
import com.example.basketballshoesandroidshop.databinding.ActivityCartAcitivityBinding;
import com.example.basketballshoesandroidshop.Utils.SessionManager;
import com.example.basketballshoesandroidshop.Domain.User;

import java.util.ArrayList;
import java.util.List;

public class CartAcitivity extends AppCompatActivity {

    private ActivityCartAcitivityBinding biding;
    private double tax;
    private ManagmentCart managementCart;
    private MainRepository repository;
    Button btnCheckout;
    TextView total;
    String userId;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        biding = ActivityCartAcitivityBinding.inflate(getLayoutInflater());
        setContentView(biding.getRoot());

        managementCart = new ManagmentCart(this);
        repository = new MainRepository();
        sessionManager = new SessionManager(this);
        User currentUser = sessionManager.getUserFromSession();
        if (currentUser != null && currentUser.getUserId() != null) {
            userId = currentUser.getUserId();
        } else {
            // fallback nếu không có session, có thể chuyển về Login hoặc dùng id mặc định
            userId = "user_001";
        }
        CalculatorCart();
        SetVariable();
        initCartList();
        btnCheckout = findViewById(R.id.checkoutBtn);
        total = findViewById(R.id.totalTxt);
        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (total == null || total.getText().toString().isEmpty()) {
                    Toast.makeText(CartAcitivity.this, "Không có sản phẩm nào trong giỏ", Toast.LENGTH_SHORT).show();
                    return;
                }

                String totalString = total.getText().toString().replaceAll("[^\\d.]", "");
                double totalDouble = Double.parseDouble(totalString);
                Intent intent = new Intent(CartAcitivity.this, OrderPayment.class);
                intent.putExtra("total", totalDouble);
                startActivity(intent);
            }
        });

    }

    private void initCartList() {
        Log.d("CartDebug", "Loading cart for user: " + userId);

        repository.getCartWithUserId(userId).observe(this, cartList -> {
            if (cartList == null || cartList.isEmpty()) {
                Log.d("CartDebug", "Cart is empty or null");
                biding.emptyTxt.setVisibility(View.VISIBLE);
                biding.scrollView2.setVisibility(View.GONE);
                return;
            }

            Log.d("CartDebug", "Cart items fetched: " + cartList.size());
            biding.emptyTxt.setVisibility(View.GONE);
            biding.scrollView2.setVisibility(View.VISIBLE);

            List<String> itemIds = new ArrayList<>();
            for (CartItemModel cartItem : cartList) {
                itemIds.add(cartItem.getItemId());
            }

            Log.d("CartDebug", "Item IDs to fetch: " + itemIds);

            repository.getItemsByIds(itemIds).observe(this, items -> {
                if (items == null || items.isEmpty()) {
                    Log.d("CartDebug", "No ItemsModel found for these IDs");
                    return;
                }

                Log.d("CartDebug", "ItemsModel fetched: " + items.size());

                ArrayList<ItemsModel> displayItems = new ArrayList<>();
                for (ItemsModel item : items) {
                    for (CartItemModel cartItem : cartList) {
                        if (item.getId().equals(cartItem.getItemId())) {
                            item.setNumberInCart(cartItem.getQuantity());
                            item.setSelectedColor(cartItem.getColor());
                            item.setSelectedSize(cartItem.getSize());
                            displayItems.add(item);

                            Log.d("CartDebug", "Mapped item: " + item.getTitle() + " - Qty: " + cartItem.getQuantity());
                            break;
                        }
                    }
                }

                Log.d("CartDebug", "Display items for adapter: " + displayItems.size());

                biding.cartView.setLayoutManager(new LinearLayoutManager(this));
                biding.cartView.setAdapter(new CartAdapter(displayItems, this::CalculatorCart, userId, repository));
            });
        });
    }

    private void SetVariable() {
        biding.backBtn.setOnClickListener(v -> finish());
    }

    private void CalculatorCart() {
        double delivery = 10;
        double percentTax = 0.02;

        // Lấy tổng giá từ Firebase
        repository.getCartTotal(userId).observe(this, itemTotal -> {
            if (itemTotal == null)
                itemTotal = 0.0;

            double tax = itemTotal * percentTax;
            double total = Math.round((itemTotal + delivery + tax) * 100.0) / 100.0;

            // Cập nhật giao diện
            biding.totalFeeTxt.setText("$" + String.format("%.2f", itemTotal));
            biding.deliveryTxt.setText("$" + String.format("%.2f", delivery));
            biding.totalTxt.setText("$" + String.format("%.2f", total));
        });
    }

}