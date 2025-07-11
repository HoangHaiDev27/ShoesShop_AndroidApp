package com.example.basketballshoesandroidshop.Repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.basketballshoesandroidshop.Domain.BannerModel;
import com.example.basketballshoesandroidshop.Domain.CategoryModel;
import com.example.basketballshoesandroidshop.Domain.ItemsModel;
import com.example.basketballshoesandroidshop.Domain.WishlistModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

public class MainRepository {
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    public LiveData<ArrayList<CategoryModel>> loadCategory() {
        MutableLiveData<ArrayList<CategoryModel>> listData = new MutableLiveData<>();
        DatabaseReference ref = firebaseDatabase.getReference("Category");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<CategoryModel> list = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    CategoryModel item = childSnapshot.getValue(CategoryModel.class);
                    if (item != null) list.add(item);
                }
                listData.setValue(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return listData;
    }

    public LiveData<ArrayList<BannerModel>> loadBanner() {
        MutableLiveData<ArrayList<BannerModel>> listData = new MutableLiveData<>();
        DatabaseReference ref = firebaseDatabase.getReference("Banner");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<BannerModel> list = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    BannerModel item = childSnapshot.getValue(BannerModel.class);
                    if (item != null) list.add(item);
                }
                listData.setValue(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return listData;
    }

    public LiveData<ArrayList<ItemsModel>> loadPopular() {
        MutableLiveData<ArrayList<ItemsModel>> listData = new MutableLiveData<>();
        DatabaseReference ref = firebaseDatabase.getReference("Items");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<ItemsModel> list = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    ItemsModel item = childSnapshot.getValue(ItemsModel.class);
                    if (item != null) {
                        // Set id cho item từ key hoặc từ field id có sẵn
                        if (item.getId() == null || item.getId().isEmpty()) {
                            item.setId(childSnapshot.getKey()); // fallback to using Firebase key
                        }
                        list.add(item);
                    }
                }
                listData.setValue(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return listData;
    }

    // Wishlist CRUD methods
    public Task<Void> addToWishlist(String userId, String itemId) {
        DatabaseReference wishlistRef = firebaseDatabase.getReference("Wishlist").child(userId).child(itemId);
        WishlistModel wishlistItem = new WishlistModel(itemId, System.currentTimeMillis());
        return wishlistRef.setValue(wishlistItem);
    }

    public Task<Void> removeFromWishlist(String userId, String itemId) {
        DatabaseReference wishlistRef = firebaseDatabase.getReference("Wishlist").child(userId).child(itemId);
        return wishlistRef.removeValue();
    }

    public LiveData<ArrayList<String>> getUserWishlist(String userId) {
        MutableLiveData<ArrayList<String>> listData = new MutableLiveData<>();
        DatabaseReference ref = firebaseDatabase.getReference("Wishlist").child(userId);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> itemIds = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    WishlistModel item = childSnapshot.getValue(WishlistModel.class);
                    if (item != null) itemIds.add(item.getItemId());
                }
                listData.setValue(itemIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return listData;
    }

    public LiveData<Boolean> isInWishlist(String userId, String itemId) {
        MutableLiveData<Boolean> isInWishlist = new MutableLiveData<>();
        DatabaseReference ref = firebaseDatabase.getReference("Wishlist").child(userId).child(itemId);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isInWishlist.setValue(snapshot.exists());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                isInWishlist.setValue(false);
            }
        });
        return isInWishlist;
    }
    
    // Method to check wishlist status only once (for click events)
    public void checkWishlistStatus(String userId, String itemId, WishlistStatusCallback callback) {
        DatabaseReference ref = firebaseDatabase.getReference("Wishlist").child(userId).child(itemId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callback.onResult(snapshot.exists());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onResult(false);
            }
        });
    }
    
    // Callback interface for single check
    public interface WishlistStatusCallback {
        void onResult(boolean isInWishlist);
    }

    public LiveData<ArrayList<ItemsModel>> getWishlistItems(String userId) {
        MutableLiveData<ArrayList<ItemsModel>> listData = new MutableLiveData<>();

        // Đầu tiên lấy danh sách itemId từ wishlist
        DatabaseReference wishlistRef = firebaseDatabase.getReference("Wishlist").child(userId);
        wishlistRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<ItemsModel> items = new ArrayList<>();

                if (!snapshot.exists()) {
                    listData.setValue(items);
                    return;
                }

                // Đếm số lượng items cần fetch
                long totalItems = snapshot.getChildrenCount();
                if (totalItems == 0) {
                    listData.setValue(items);
                    return;
                }

                // Fetch từng item từ Items node
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    WishlistModel wishlistItem = childSnapshot.getValue(WishlistModel.class);
                    if (wishlistItem != null) {
                        DatabaseReference itemRef = firebaseDatabase.getReference("Items");
                        itemRef.orderByChild("id").equalTo(wishlistItem.getItemId())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot itemSnapshot) {
                                for (DataSnapshot itemChild : itemSnapshot.getChildren()) {
                                    ItemsModel item = itemChild.getValue(ItemsModel.class);
                                    if (item != null) {
                                        // Set id cho item từ key hoặc từ field id có sẵn
                                        if (item.getId() == null || item.getId().isEmpty()) {
                                            item.setId(itemChild.getKey()); // fallback to using Firebase key
                                        }
                                        items.add(item);
                                    }
                                }
                                // Cập nhật khi đã fetch xong tất cả items
                                if (items.size() == totalItems) {
                                    listData.setValue(items);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listData.setValue(new ArrayList<>());
            }
        });

        return listData;
    }
}
