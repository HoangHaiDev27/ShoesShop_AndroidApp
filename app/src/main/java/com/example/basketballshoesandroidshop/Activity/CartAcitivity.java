package com.example.basketballshoesandroidshop.Activity;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.basketballshoesandroidshop.Helper.ChangeNumberItemsListener;
import com.example.basketballshoesandroidshop.Helper.ManagmentCart;
import com.example.basketballshoesandroidshop.R;
import com.example.basketballshoesandroidshop.databinding.ActivityCartAcitivityBinding;

public class CartAcitivity extends AppCompatActivity {

    private ActivityCartAcitivityBinding biding;
    private double tax;
    private ManagmentCart managementCart;
    Button  btnCheckout;
    TextView total;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        biding = ActivityCartAcitivityBinding.inflate(getLayoutInflater());
        setContentView(biding.getRoot());

        managementCart = new ManagmentCart(this);
        CalculatorCart();
        SetVariable();
        initCartList();
        btnCheckout = findViewById(R.id.checkoutBtn);
        total = findViewById(R.id.totalTxt);
        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(total ==  null ||total.getText().toString().isEmpty()){
                    Toast.makeText(CartAcitivity.this,"Không có sản phẩm nào trong giỏ",Toast.LENGTH_SHORT).show();
                    return;
                }

                String totalString = total.getText().toString().replaceAll("[^\\d.]", "");
                double totalDouble = Double.parseDouble(totalString);
                Intent intent = new Intent(CartAcitivity.this, OrderPayment.class);
                intent.putExtra("total",totalDouble);
                startActivity(intent);
            }
        });

    }

    private void initCartList() {
        if(managementCart.getListCart().isEmpty()){
            biding.emptyTxt.setVisibility(View.VISIBLE);
            biding.scrollView2.setVisibility(View.GONE);
        }else{
            biding.emptyTxt.setVisibility(View.GONE);
            biding.scrollView2.setVisibility(View.VISIBLE);
        }
        biding.cartView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        biding.cartView.setAdapter(
                new CartAdapter(
                        managementCart.getListCart(),
                        () -> CalculatorCart(),
                        this // đây là context
                )
        );

    }

    private void SetVariable() {
        biding.backBtn.setOnClickListener(v->finish());
    }

    private void CalculatorCart() {
        double percentTax = 0.02;
        double delivey = 10;
        double total = Math.round((managementCart.getTotalFee() + delivey)*100.0)/100.0;
        double itemTotal = Math.round((managementCart.getTotalFee()*100.0))/100;
        biding.totalFeeTxt.setText("$" +itemTotal);
        biding.deliveryTxt.setText("$"+delivey);
        biding.totalTxt.setText("$"+total);
    }
}