package com.example.basketballshoesandroidshop.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.basketballshoesandroidshop.databinding.ActivityLoginBinding;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.loginBtn.setOnClickListener(v -> {
            String username = binding.usernameEt.getText().toString().trim();
            String password = binding.passwordEt.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (validateLogin(username, password)) {
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        });

        binding.signupTxt.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        });

        binding.skipTxt.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        });
    }

    private boolean validateLogin(String username, String password) {
        try {
            FileInputStream fstream = openFileInput("user_details");
            StringBuffer sbuffer = new StringBuffer();
            int i;
            while ((i = fstream.read()) != -1) {
                sbuffer.append((char) i);
            }
            fstream.close();

            String[] details = sbuffer.toString().split("\n");
            if (details.length >= 2) {
                return username.equals(details[0]) && password.equals(details[1]);
            }
        } catch (FileNotFoundException e) {
            // No user registered yet
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}