package com.example.basketballshoesandroidshop.Utils;

import java.util.Set;
import java.util.HashSet;

public class WishlistCache {
    private static WishlistCache instance;
    private Set<String> wishlistItems;
    
    private WishlistCache() {
        wishlistItems = new HashSet<>();
    }
    
    public static synchronized WishlistCache getInstance() {
        if (instance == null) {
            instance = new WishlistCache();
        }
        return instance;
    }
    
    public boolean isInWishlist(String itemId) {
        return wishlistItems.contains(itemId);
    }
    
    public void addToWishlist(String itemId) {
        wishlistItems.add(itemId);
    }
    
    public void removeFromWishlist(String itemId) {
        wishlistItems.remove(itemId);
    }
    
    public void clear() {
        wishlistItems.clear();
    }
} 