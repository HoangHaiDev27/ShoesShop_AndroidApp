package com.example.basketballshoesandroidshop.Activity;

import android.os.Bundle;
import android.view.View;

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

    }

    private void initCartList() {
        if(managementCart.getListCart().isEmpty()){
            biding.emptyTxt.setVisibility(View.VISIBLE);
            biding.scrollView2.setVisibility(View.GONE);
        }else{
            biding.emptyTxt.setVisibility(View.GONE);
            biding.scrollView2.setVisibility(View.VISIBLE);
        }
        biding.cartView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
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
        tax = Math.round((managementCart.getTotalFee() * percentTax * 100.0))/100.0;

        double total = Math.round((managementCart.getTotalFee() + tax + delivey)*100.0)/100.0;
        double itemTotal = Math.round((managementCart.getTotalFee()*100.0))/100;
        biding.totalFeeTxt.setText("$" +itemTotal);
        biding.taxTxt.setText("$"+delivey);
        biding.deliveryTxt.setText("$"+delivey);
        biding.totalTxt.setText("$"+total);
    }
}