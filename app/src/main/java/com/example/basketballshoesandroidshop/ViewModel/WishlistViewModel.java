package com.example.basketballshoesandroidshop.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.basketballshoesandroidshop.Domain.ItemsModel;
import com.example.basketballshoesandroidshop.Repository.MainRepository;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

public class WishlistViewModel extends ViewModel {
    private final MainRepository repository = new MainRepository();
    
    public Task<Void> addToWishlist(String userId, String itemId) {
        return repository.addToWishlist(userId, itemId);
    }
    
    public Task<Void> removeFromWishlist(String userId, String itemId) {
        return repository.removeFromWishlist(userId, itemId);
    }
    
    public LiveData<ArrayList<String>> getUserWishlist(String userId) {
        return repository.getUserWishlist(userId);
    }
    
    public LiveData<Boolean> isInWishlist(String userId, String itemId) {
        return repository.isInWishlist(userId, itemId);
    }
    
    public LiveData<ArrayList<ItemsModel>> getWishlistItems(String userId) {
        return repository.getWishlistItems(userId);
    }
    
    public void checkWishlistStatus(String userId, String itemId, MainRepository.WishlistStatusCallback callback) {
        repository.checkWishlistStatus(userId, itemId, callback);
    }
} 