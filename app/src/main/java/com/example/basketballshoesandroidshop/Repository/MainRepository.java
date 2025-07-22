package com.example.basketballshoesandroidshop.Repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.basketballshoesandroidshop.Domain.BannerModel;
import com.example.basketballshoesandroidshop.Domain.CartItemModel;
import com.example.basketballshoesandroidshop.Domain.CategoryModel;
import com.example.basketballshoesandroidshop.Domain.ItemsModel;
import com.example.basketballshoesandroidshop.Domain.WishlistModel;
import com.example.basketballshoesandroidshop.Domain.OrderModel;
import com.example.basketballshoesandroidshop.Domain.OrderItemModel;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MainRepository {
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private final DatabaseReference databaseReference = firebaseDatabase.getReference();

    public LiveData<ArrayList<CategoryModel>> loadCategory() {
        MutableLiveData<ArrayList<CategoryModel>> listData = new MutableLiveData<>();
        DatabaseReference ref = firebaseDatabase.getReference("Category");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<CategoryModel> list = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    CategoryModel item = childSnapshot.getValue(CategoryModel.class);
                    if (item != null)
                        list.add(item);
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
                    if (item != null)
                        list.add(item);
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
                    if (item != null)
                        itemIds.add(item.getItemId());
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

    public Task<Void> addToCart(String userId, CartItemModel newItem) {
        DatabaseReference cartUserRef = firebaseDatabase.getReference("Cart").child(userId);
        DatabaseReference cartItemRef = cartUserRef.child(newItem.getItemId());

        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();

        Log.d("CartDebug", "addToCart called for user: " + userId + ", item: " + newItem.getItemId());
        cartUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userCartSnapshot) {
                if (!userCartSnapshot.exists()) {
                    Log.d("CartDebug",
                            "User cart does not exist. Creating cart and adding item: " + newItem.getItemId());
                    cartItemRef.setValue(newItem)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("CartDebug", "Successfully created cart and added item: " + newItem.getItemId());
                                taskCompletionSource.setResult(aVoid);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("CartDebug", "Failed to create cart/add item: " + e.getMessage());
                                taskCompletionSource.setException(e);
                            });
                } else {
                    Log.d("CartDebug", "User cart exists. Checking item: " + newItem.getItemId());
                    cartItemRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Log.d("CartDebug",
                                        "Item already exists in cart. Increasing quantity: " + newItem.getItemId());
                                CartItemModel existingItem = snapshot.getValue(CartItemModel.class);
                                if (existingItem != null) {
                                    int updatedQuantity = existingItem.getQuantity() + 1;
                                    existingItem.setQuantity(updatedQuantity);
                                    cartItemRef.setValue(existingItem)
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d("CartDebug", "Successfully updated quantity for item: "
                                                        + newItem.getItemId());
                                                taskCompletionSource.setResult(aVoid);
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("CartDebug", "Failed to update quantity: " + e.getMessage());
                                                taskCompletionSource.setException(e);
                                            });
                                } else {
                                    Log.e("CartDebug",
                                            "Existing item is null, overwriting with new item: " + newItem.getItemId());
                                    cartItemRef.setValue(newItem)
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d("CartDebug",
                                                        "Successfully overwrote null item: " + newItem.getItemId());
                                                taskCompletionSource.setResult(aVoid);
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("CartDebug", "Failed to overwrite null item: " + e.getMessage());
                                                taskCompletionSource.setException(e);
                                            });
                                }
                            } else {
                                Log.d("CartDebug",
                                        "Item does not exist in cart. Adding new item: " + newItem.getItemId());
                                cartItemRef.setValue(newItem)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("CartDebug", "Successfully added new item: " + newItem.getItemId());
                                            taskCompletionSource.setResult(aVoid);
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("CartDebug", "Failed to add new item: " + e.getMessage());
                                            taskCompletionSource.setException(e);
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("CartDebug", "Error checking item in cart: " + error.getMessage());
                            taskCompletionSource.setException(error.toException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CartDebug", "Error checking user cart: " + error.getMessage());
                taskCompletionSource.setException(error.toException());
            }
        });

        return taskCompletionSource.getTask();
    }

    // Xóa sản phẩm khỏi giỏ hàng
    public Task<Void> deleteFromCart(String userId, String itemId) {
        DatabaseReference cartRef = firebaseDatabase.getReference("Cart").child(userId).child(itemId);
        return cartRef.removeValue();
    }

    // Tạo giỏ hàng rỗng cho user nếu chưa có
    public void createEmptyCartForUser(String userId) {
        DatabaseReference cartRef = firebaseDatabase.getReference("Cart").child(userId);
        cartRef.setValue(new ArrayList<CartItemModel>());
    }

    // load cart with user id
    public LiveData<List<CartItemModel>> getCartWithUserId(String userId) {
        MutableLiveData<List<CartItemModel>> liveData = new MutableLiveData<>();

        Log.d("CartDebug", "Fetching cart for user: " + userId);

        databaseReference.child("Cart").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<CartItemModel> list = new ArrayList<>();
                        Log.d("CartDebug", "Snapshot children count: " + snapshot.getChildrenCount());

                        for (DataSnapshot itemSnap : snapshot.getChildren()) {
                            CartItemModel item = itemSnap.getValue(CartItemModel.class);
                            if (item != null) {
                                list.add(item);
                                Log.d("CartDebug", "Item loaded: " + item.getItemId() + ", qty: " + item.getQuantity());
                            } else {
                                Log.d("CartDebug", "Null CartItemModel found");
                            }
                        }

                        liveData.setValue(list);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("CartDebug", "Firebase error: " + error.getMessage());
                        liveData.setValue(null);
                    }
                });

        return liveData;
    }

    // Giả sử bạn đã có model CartItemModel với các trường như price và quantity
    public LiveData<Double> getCartTotal(String userId) {
        MutableLiveData<Double> totalLiveData = new MutableLiveData<>();
        LiveData<List<CartItemModel>> cartLiveData = getCartWithUserId(userId);

        Observer<List<CartItemModel>> observer = new Observer<List<CartItemModel>>() {
            @Override
            public void onChanged(List<CartItemModel> cartItems) {
                double total = 0;
                for (CartItemModel item : cartItems) {
                    double price = item.getPrice() != 0 ? item.getPrice() : 0;
                    int quantity = item.getQuantity() != 0 ? item.getQuantity() : 0;
                    total += price * quantity;
                }
                totalLiveData.setValue(total);
                cartLiveData.removeObserver(this); // 👈 Rất quan trọng để tránh memory leak
            }
        };

        cartLiveData.observeForever(observer);
        return totalLiveData;
    }

    public LiveData<ArrayList<ItemsModel>> getItemsByIds(List<String> itemIds) {
        MutableLiveData<ArrayList<ItemsModel>> liveData = new MutableLiveData<>();
        databaseReference.child("Items").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<ItemsModel> result = new ArrayList<>();
                for (DataSnapshot itemSnap : snapshot.getChildren()) {
                    ItemsModel item = itemSnap.getValue(ItemsModel.class);
                    if (item != null && itemIds.contains(item.getId())) {
                        result.add(item);
                    }
                }
                liveData.setValue(result);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                liveData.setValue(null);
            }
        });
        return liveData;
    }

    public Task<Void> updateCartItemQuantity(String userId, String itemId, int quantity) {
        return firebaseDatabase.getReference("Cart")
                .child(userId)
                .child(itemId)
                .child("quantity")
                .setValue(quantity);
    }

    public Task<Void> removeItemFromCart(String userId, String itemId) {
        return firebaseDatabase.getReference("Cart")
                .child(userId)
                .child(itemId)
                .removeValue();
    }

    /**
     * Lấy danh sách đơn hàng theo trạng thái và userId
     */
    public LiveData<ArrayList<OrderModel>> getOrdersByStatus(String userId, String status) {
        MutableLiveData<ArrayList<OrderModel>> listData = new MutableLiveData<>();

        Query query = databaseReference.child("Orders").child(userId)
                .orderByChild("orderStatus")
                .equalTo(status);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<OrderModel> orderList = new ArrayList<>();

                if (!dataSnapshot.exists()) {
                    listData.setValue(orderList);
                    return;
                }

                final long totalOrders = dataSnapshot.getChildrenCount();
                final AtomicInteger ordersProcessed = new AtomicInteger(0);

                for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                    OrderModel order = orderSnapshot.getValue(OrderModel.class);
                    String orderId = orderSnapshot.getKey();

                    if (order != null) {
                        order.setOrderId(orderId);

                        // Lấy OrderItems cho đơn hàng này
                        DatabaseReference itemsRef = databaseReference.child("OrderItem").child(orderId);
                        itemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot itemsSnapshot) {
                                List<OrderItemModel> items = new ArrayList<>();
                                for (DataSnapshot itemData : itemsSnapshot.getChildren()) {
                                    OrderItemModel item = itemData.getValue(OrderItemModel.class);
                                    if (item != null) {
                                        items.add(item);
                                    }
                                }
                                order.setItems(items);
                                orderList.add(order);

                                if (ordersProcessed.incrementAndGet() == totalOrders) {
                                    listData.setValue(orderList);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                if (ordersProcessed.incrementAndGet() == totalOrders) {
                                    listData.setValue(orderList);
                                }
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

    /**
     * Lấy danh sách đơn hàng "Đã giao" để hiển thị button mua lại
     */
    public LiveData<ArrayList<OrderModel>> getDeliveredOrders(String userId) {
        return getOrdersByStatus(userId, "Đã giao");
    }

    /**
     * Lấy chi tiết OrderItems của một đơn hàng
     */
    public LiveData<ArrayList<OrderItemModel>> getOrderItems(String orderId) {
        MutableLiveData<ArrayList<OrderItemModel>> listData = new MutableLiveData<>();

        DatabaseReference itemsRef = databaseReference.child("OrderItem").child(orderId);
        itemsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<OrderItemModel> items = new ArrayList<>();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    OrderItemModel item = itemSnapshot.getValue(OrderItemModel.class);
                    if (item != null) {
                        items.add(item);
                    }
                }
                listData.setValue(items);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listData.setValue(new ArrayList<>());
            }
        });

        return listData;
    }

    /**
     * Thêm nhiều OrderItems vào giỏ hàng (cho tính năng mua lại)
     */
    public Task<Void> addOrderItemsToCart(String userId, List<OrderItemModel> orderItems) {
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();

        if (orderItems == null || orderItems.isEmpty()) {
            taskCompletionSource.setResult(null);
            return taskCompletionSource.getTask();
        }

        List<Task<Void>> tasks = new ArrayList<>();

        for (OrderItemModel orderItem : orderItems) {
            // Chuyển đổi OrderItemModel thành CartItemModel
            CartItemModel cartItem = new CartItemModel(
                    orderItem.getItemId(),
                    orderItem.getPrice(),
                    orderItem.getQuantity(),
                    orderItem.getSize(),
                    "" // OrderItemModel không có color, để trống hoặc lấy từ database
            );

            // Thêm vào giỏ hàng
            Task<Void> addTask = addToCart(userId, cartItem);
            tasks.add(addTask);
        }

        // Đợi tất cả tasks hoàn thành
        Tasks.whenAll(tasks)
                .addOnSuccessListener(aVoid -> taskCompletionSource.setResult(null))
                .addOnFailureListener(taskCompletionSource::setException);

        return taskCompletionSource.getTask();
    }

    /**
     * Kiểm tra trạng thái sản phẩm trước khi mua lại
     */
    public Task<Boolean> checkItemAvailability(String itemId) {
        TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();

        DatabaseReference itemRef = databaseReference.child("Items").child(itemId);
        itemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isAvailable = snapshot.exists();
                taskCompletionSource.setResult(isAvailable);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                taskCompletionSource.setResult(false);
            }
        });

        return taskCompletionSource.getTask();
    }

}
