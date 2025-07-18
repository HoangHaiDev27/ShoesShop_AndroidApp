package com.example.basketballshoesandroidshop.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;

import com.example.basketballshoesandroidshop.Adapter.CartAdapter;
import com.example.basketballshoesandroidshop.Adapter.CategoryAdapter;
import com.example.basketballshoesandroidshop.Adapter.PopularAdapter;
import com.example.basketballshoesandroidshop.Adapter.SliderAdapter;
import com.example.basketballshoesandroidshop.Domain.BannerModel;
import com.example.basketballshoesandroidshop.R;
import com.example.basketballshoesandroidshop.ViewModel.MainViewModel;
import com.example.basketballshoesandroidshop.databinding.ActivityMainBinding;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private EditText edtSearch;
    private CategoryAdapter categoryAdapter; // Lưu reference đến CategoryAdapter
    private PopularAdapter popularAdapter; // Lưu reference đến PopularAdapter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new MainViewModel();

        initCategory();
        initSlider();
        initPopular();
        bottomNavigation();
        navigateToCatalog();
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    private void navigateToCatalog() {
        edtSearch = findViewById(R.id.editTextText); // ID của ô tìm kiếm trong layout

        // Lắng nghe sự kiện nhấn vào ô tìm kiếm
        edtSearch.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CatalogActivity.class);
            startActivity(intent); // Mở Activity tìm kiếm
        });
    }

    private void bottomNavigation() {
        binding.bottomNavigation.setItemSelected(R.id.home, true); // đặt mặc định

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
                // Tạo và lưu reference đến PopularAdapter
                popularAdapter = new PopularAdapter(itemsModels);
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
            
            // Tạo và lưu reference đến CategoryAdapter
            categoryAdapter = new CategoryAdapter(categoryModels);
            binding.categoryView.setAdapter(categoryAdapter);
            binding.categoryView.setNestedScrollingEnabled(true);
            binding.progressBarCategory.setVisibility(View.GONE);
        });
    }

}