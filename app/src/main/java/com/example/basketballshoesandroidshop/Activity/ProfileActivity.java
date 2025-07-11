package com.example.basketballshoesandroidshop.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.basketballshoesandroidshop.R;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        RelativeLayout rlPurchaseHistory = findViewById(R.id.rlPurchaseHistory);
        rlPurchaseHistory.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, OrderHistoryActivity.class);
            startActivity(intent);
        });
    }
}
