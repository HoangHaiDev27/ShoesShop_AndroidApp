package com.example.basketballshoesandroidshop.ViewModel;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.basketballshoesandroidshop.Domain.ItemsModel;
import com.example.basketballshoesandroidshop.Domain.VariationModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
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

    public void forceGet(int categoryId, int option, String searchContent) {
        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference("Items");
        Query query = categoryId == 0 ? productsRef : productsRef.orderByChild("categoryId").equalTo(categoryId);

        this.option = option;
        String opt = "";
        boolean ascending = true;

        switch (option) {
            case 0:
                opt = "price";
                ascending = false;
                break;
            case 1:
                opt = "price";
                ascending = true;
                break;
            case 2:
                opt = "title";
                ascending = false;
                break;
            case 3:
                opt = "title";
                ascending = true;
                break;
        }

        Log.e(TAG, "forceGet: ordering by " + opt + " ascending=" + ascending);

        final String optInner = opt;
        boolean ascendingInner = ascending;
        // Apply ordering inside result processing
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ItemsModel> tempProducts = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    ItemsModel product = child.getValue(ItemsModel.class);
                    if (product != null) {
                        if (!searchContent.isEmpty() && !product.getTitle().toLowerCase().contains(searchContent)) {
                            continue; // bỏ qua nếu không khớp
                        }
                        if (product.getDescription().length() > 55) {
                            product.setDescription(product.getDescription().substring(0, 55 - 3) + "...");
                        }
                        tempProducts.add(product);
                    }
                }

                // Sort manually since Firebase Realtime DB only allows 1 orderBy* per query
                Collections.sort(tempProducts, (p1, p2) -> {
                    if (optInner.equals("price")) {
                        return ascendingInner ? Double.compare(p1.getPrice(), p2.getPrice())
                                : Double.compare(p2.getPrice(), p1.getPrice());
                    } else if (optInner.equals("title")) {
                        return ascendingInner ? p1.getTitle().compareToIgnoreCase(p2.getTitle())
                                : p2.getTitle().compareToIgnoreCase(p1.getTitle());
                    }
                    return 0;
                });

                products = tempProducts;
                productsLiveData.setValue(products);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "forceGet: onCancelled: " + error.getMessage());
            }
        });
    }

    public void forceGet(int categoryId, int option, double minPrice, double maxPrice, List<VariationModel> variationModels, String searchContent) {
        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference("Items");
        Query query = categoryId == 0 ? productsRef : productsRef.orderByChild("categoryId").equalTo(categoryId);

        this.option = option;
        String opt = "";
        boolean ascending = true;

        switch (option) {
            case 0:
                opt = "price";
                ascending = false;
                break;
            case 1:
                opt = "price";
                ascending = true;
                break;
            case 2:
                opt = "title";
                ascending = false;
                break;
            case 3:
                opt = "title";
                ascending = true;
                break;
        }

        Log.e(TAG, "forceGet: ordering by " + opt + " ascending=" + ascending);

        final String optInner = opt;
        boolean ascendingInner = ascending;
        // Apply ordering inside result processing
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ItemsModel> tempProducts = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    ItemsModel product = child.getValue(ItemsModel.class);
                    if (product != null) {
                        if (!searchContent.isEmpty() && !product.getTitle().toLowerCase().contains(searchContent)) {
                            continue; // bỏ qua nếu không khớp
                        }
                        if (product.getPrice() < minPrice || product.getPrice() > maxPrice) {
                            continue;
                        }
                        for (VariationModel variationModel : variationModels) {
                            if (variationModel.inventory == 0) {
                                continue;
                            }
                        }
                        if (product.getDescription().length() > 55) {
                            product.setDescription(product.getDescription().substring(0, 55 - 3) + "...");
                        }
                        tempProducts.add(product);
                    }
                }

                // Sort manually since Firebase Realtime DB only allows 1 orderBy* per query
                Collections.sort(tempProducts, (p1, p2) -> {
                    if (optInner.equals("price")) {
                        return ascendingInner ? Double.compare(p1.getPrice(), p2.getPrice())
                                : Double.compare(p2.getPrice(), p1.getPrice());
                    } else if (optInner.equals("title")) {
                        return ascendingInner ? p1.getTitle().compareToIgnoreCase(p2.getTitle())
                                : p2.getTitle().compareToIgnoreCase(p1.getTitle());
                    }
                    return 0;
                });

                products = tempProducts;
                productsLiveData.setValue(products);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "forceGet: onCancelled: " + error.getMessage());
            }
        });
    }

}
