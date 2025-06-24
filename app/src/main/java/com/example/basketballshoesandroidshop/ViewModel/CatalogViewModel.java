package com.example.basketballshoesandroidshop.ViewModel;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.basketballshoesandroidshop.Domain.ItemsModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CatalogViewModel extends ViewModel {
    private static final String TAG = "CatalogViewModel";

    private MutableLiveData<List<ItemsModel>> productsLiveData;
    private List<ItemsModel> products;
    private int option = 0;
    private boolean isDataLoaded = false;

    public CatalogViewModel() {
        init();
    }

    private void init() {
        products = new ArrayList<>();
        productsLiveData = new MutableLiveData<>();
    }

    public LiveData<List<ItemsModel>> getAllProductsByCategory(int categoryId) {
        if (isDataLoaded) {
            return productsLiveData;  // Return existing data if already loaded
        }

        forceGet(categoryId);

        return productsLiveData;
    }

    public void forceGet(int categoryId) {
        if (isDataLoaded) return;
        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference("Items");

        Query query = categoryId == 0 ? productsRef : productsRef.orderByChild("categoryId").equalTo(categoryId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ItemsModel> products = new ArrayList<>();

                // Lặp qua các sản phẩm và thêm vào danh sách
                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    try {
                        ItemsModel product = productSnapshot.getValue(ItemsModel.class);
                        if (product != null) {
                            // Xử lý mô tả sản phẩm nếu cần
                            if (product.getDescription().length() > 55) {
                                product.setDescription(product.getDescription().substring(0, 55 - 3) + "...");
                            }
                            products.add(product);
                        }
                    } catch (Exception e) {
                        Log.d(TAG, "getAllProductsByCategory: " + e.getMessage());
                    }
                }
                // Cập nhật LiveData với danh sách sản phẩm
                productsLiveData.setValue(products);
                isDataLoaded = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "getCategoryList:failure: " + error.getMessage());
            }
        });
    }


}
