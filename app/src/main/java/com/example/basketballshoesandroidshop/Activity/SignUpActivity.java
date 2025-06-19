package com.example.basketballshoesandroidshop.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.basketballshoesandroidshop.databinding.ActivitySignupBinding;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.signupBtn.setOnClickListener(v -> {
            String username = binding.usernameEt.getText().toString().trim();
            String email = binding.emailEt.getText().toString().trim();
            String password = binding.passwordEt.getText().toString().trim();
            String confirmPassword = binding.confirmPasswordEt.getText().toString().trim();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            saveUserData(username, email, password);
        });

        binding.loginTxt.setOnClickListener(v -> {
            finish();
        });

        binding.backBtn.setOnClickListener(v -> {
            finish();
        });
    }

    private void saveUserData(String username, String email, String password) {
        try {
            FileOutputStream fstream = openFileOutput("user_details", Context.MODE_PRIVATE);
            String userData = username + "\n" + password + "\n" + email;
            fstream.write(userData.getBytes());
            fstream.close();

            Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating account", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating account", Toast.LENGTH_SHORT).show();
        }
    }
}