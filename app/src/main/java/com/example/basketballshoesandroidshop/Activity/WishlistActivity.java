package com.example.basketballshoesandroidshop.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.basketballshoesandroidshop.Adapter.WishlistAdapter;
import com.example.basketballshoesandroidshop.Domain.ItemsModel;
import com.example.basketballshoesandroidshop.R;
import com.example.basketballshoesandroidshop.ViewModel.WishlistViewModel;
import com.example.basketballshoesandroidshop.databinding.ActivityWishlistBinding;

import java.util.ArrayList;

public class WishlistActivity extends AppCompatActivity {
    private ActivityWishlistBinding binding;
    private WishlistViewModel viewModel;
    private WishlistAdapter adapter;
    private String currentUserId = "user_001"; // Tạm thời hardcode, sau này sẽ lấy từ session
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWishlistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        viewModel = new WishlistViewModel();
        
        initViews();
        setupRecyclerView();
        loadWishlistItems();
    }
    
    private void initViews() {
        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Danh sách yêu thích");
        }
        
        // Back button listener
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void setupRecyclerView() {
        adapter = new WishlistAdapter(new ArrayList<>(), new WishlistAdapter.OnWishlistItemClickListener() {
            @Override
            public void onItemClick(ItemsModel item) {
                // Navigate to detail activity
                // Intent intent = new Intent(WishlistActivity.this, DetailActivity.class);
                // intent.putExtra("item", item);
                // startActivity(intent);
            }
            
            @Override
            public void onRemoveFromWishlist(ItemsModel item) {
                removeFromWishlist(item);
            }
        });
        
        binding.recyclerViewWishlist.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewWishlist.setAdapter(adapter);
    }
    
    private void loadWishlistItems() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.textViewEmpty.setVisibility(View.GONE);
        
        viewModel.getWishlistItems(currentUserId).observe(this, itemsModels -> {
            binding.progressBar.setVisibility(View.GONE);
            
            if (itemsModels != null && !itemsModels.isEmpty()) {
                adapter.updateItems(itemsModels);
                binding.recyclerViewWishlist.setVisibility(View.VISIBLE);
                binding.textViewEmpty.setVisibility(View.GONE);
            } else {
                binding.recyclerViewWishlist.setVisibility(View.GONE);
                binding.textViewEmpty.setVisibility(View.VISIBLE);
            }
        });
    }
    
    private void removeFromWishlist(ItemsModel item) {
        viewModel.removeFromWishlist(currentUserId, item.getId())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã xóa khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
                    // Không cần reload vì LiveData sẽ tự động cập nhật
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Có lỗi xảy ra khi xóa", Toast.LENGTH_SHORT).show();
                });
    }
} 