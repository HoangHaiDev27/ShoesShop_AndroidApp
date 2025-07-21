package com.example.basketballshoesandroidshop.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;

import com.example.basketballshoesandroidshop.Adapter.CategoryAdapter;
import com.example.basketballshoesandroidshop.Adapter.PopularAdapter;
import com.example.basketballshoesandroidshop.Adapter.SliderAdapter;
import com.example.basketballshoesandroidshop.Domain.BannerModel;
import com.example.basketballshoesandroidshop.Domain.User;
import com.example.basketballshoesandroidshop.R;
import com.example.basketballshoesandroidshop.Utils.SessionManager;
import com.example.basketballshoesandroidshop.ViewModel.MainViewModel;
import com.example.basketballshoesandroidshop.databinding.ActivityMainBinding;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private EditText edtSearch;
    private CategoryAdapter categoryAdapter;
    private PopularAdapter popularAdapter;

    // Session and User
    private SessionManager sessionManager;
    private TextView tvWelcomeBack, tvUserName;
    private LinearLayout llUserProfile;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new MainViewModel();

        // Initialize session manager
        sessionManager = new SessionManager(this);
        userId = sessionManager.getCurrentUserId();

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            goToLogin();
            return;
        }

        // Initialize views and user info
        initUserInfo();
        initCategory();
        initSlider();
        initPopular();
        bottomNavigation();
        navigateToCatalog();
        setupProfileClick();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update user info when returning to MainActivity
        updateUserInfo();

        // Đảm bảo Home tab luôn được set active khi quay về MainActivity
        if (binding != null && binding.bottomNavigation != null) {
            binding.bottomNavigation.setItemSelected(R.id.home, true);
        }

        // Reset category selection về trạng thái ban đầu
        if (categoryAdapter != null) {
            categoryAdapter.resetSelection();
        }

        // Refresh PopularAdapter để cập nhật icon heart
        if (popularAdapter != null) {
            popularAdapter.notifyDataSetChanged();
        }
    }

    private void initUserInfo() {
        // Find TextViews for user info
        tvWelcomeBack = findViewById(R.id.textView);
        tvUserName = findViewById(R.id.textView2);

        // Find the LinearLayout containing user profile
        llUserProfile = findViewById(R.id.llUserProfile); // You'll need to add this ID to layout

        // Update user info
        updateUserInfo();
    }

    private void updateUserInfo() {
        User currentUser = sessionManager.getUserFromSession();
        if (currentUser != null) {
            tvWelcomeBack.setText("Welcome back");
            tvUserName.setText(currentUser.getName());
        } else {
            // If no user found, redirect to login
            goToLogin();
        }
    }

    private void setupProfileClick() {
        // Set click listener for user profile area
        View profileArea = findViewById(R.id.llUserProfile);
        if (profileArea != null) {
            profileArea.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            });
        }

        // Also set click listener for profile image
        ImageView profileImage = findViewById(R.id.imageView2);
        if (profileImage != null) {
            profileImage.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            });
        }
    }

    private void goToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToCatalog() {
        edtSearch = findViewById(R.id.editTextText);
        edtSearch.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CatalogActivity.class);
            startActivity(intent);
        });
    }

    private void bottomNavigation() {
        binding.bottomNavigation.setItemSelected(R.id.home, true);

        binding.bottomNavigation.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int id) {
                if (id == R.id.home) {
                    // Đã ở MainActivity, không cần chuyển
                } else if (id == R.id.favorites) {
                    startActivity(new Intent(MainActivity.this, WishlistActivity.class));
                } else if (id == R.id.cart) {
                    startActivity(new Intent(MainActivity.this, CartAcitivity.class));
                } else if (id == R.id.profile) {
                    startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                }
            }
        });

        binding.cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CartAcitivity.class));
            }
        });
    }

    private void initPopular() {
        binding.progressBarPopular.setVisibility(View.VISIBLE);
        viewModel.loadPopular().observeForever(itemsModels -> {
            if(!itemsModels.isEmpty()) {
                binding.popularView.setLayoutManager(
                        new LinearLayoutManager(MainActivity.this,LinearLayoutManager.HORIZONTAL,false)
                );
                popularAdapter = new PopularAdapter(itemsModels, userId);
                binding.popularView.setAdapter(popularAdapter);
                binding.popularView.setNestedScrollingEnabled(true);
            }
            binding.progressBarPopular.setVisibility(View.GONE);
        });
        viewModel.loadPopular();
    }

    private void initSlider() {
        binding.progressBarSlider.setVisibility(View.VISIBLE);
        viewModel.loadBanner().observeForever(bannerModels -> {
            if (bannerModels != null && !bannerModels.isEmpty()) {
                banners(bannerModels);
                binding.progressBarSlider.setVisibility(View.GONE);
            }
        });
        viewModel.loadBanner();
    }

    private void banners(ArrayList<BannerModel> bannerModels) {
        binding.viewPageSlider.setAdapter(new SliderAdapter(bannerModels, binding.viewPageSlider));
        binding.viewPageSlider.setClipToPadding(false);
        binding.viewPageSlider.setClipChildren(false);
        binding.viewPageSlider.setOffscreenPageLimit(3);
        binding.viewPageSlider.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));

        binding.viewPageSlider.setPageTransformer(compositePageTransformer);
    }

    private void initCategory() {
        binding.progressBarCategory.setVisibility(View.VISIBLE);
        viewModel.loadCategory().observeForever(categoryModels -> {
            binding.categoryView.setLayoutManager(new LinearLayoutManager(
                    MainActivity.this, LinearLayoutManager.HORIZONTAL, false));

            categoryAdapter = new CategoryAdapter(categoryModels);
            binding.categoryView.setAdapter(categoryAdapter);
            binding.categoryView.setNestedScrollingEnabled(true);
            binding.progressBarCategory.setVisibility(View.GONE);
        });
    }
}