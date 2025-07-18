package com.example.basketballshoesandroidshop.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.basketballshoesandroidshop.Adapter.OrderAdapter;
import com.example.basketballshoesandroidshop.Domain.OrderItemModel;
import com.example.basketballshoesandroidshop.Domain.OrderModel;
import com.example.basketballshoesandroidshop.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderListFragment extends Fragment {

    private static final String ARG_STATUS = "status";
    private String orderStatus;

    private RecyclerView recyclerView;
    private OrderAdapter adapter;
    private List<OrderModel> orderList;
    // Thay thế FirebaseFirestore bằng DatabaseReference
    private DatabaseReference databaseReference;

    private ProgressBar progressBar;
    private TextView textViewNoOrders;

    public OrderListFragment() {
        // Required empty public constructor
    }

    public static OrderListFragment newInstance(String status) {
        OrderListFragment fragment = new OrderListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orderStatus = getArguments().getString(ARG_STATUS);
        }
        // Lấy reference đến gốc của Realtime Database
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewOrders);
        progressBar = view.findViewById(R.id.progressBar);
        textViewNoOrders = view.findViewById(R.id.textViewNoOrders);

        orderList = new ArrayList<>();
        adapter = new OrderAdapter(orderList, getContext());

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        fetchOrders();
    }

    private void fetchOrders() {
        if (orderStatus == null) {
            Log.e("FetchOrders", "orderStatus bị null! Không thể thực hiện truy vấn.");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        textViewNoOrders.setVisibility(View.GONE);

        String currentUserId = "user_001";

        Query query = databaseReference.child("Orders").child(currentUserId)
                .orderByChild("orderStatus")
                .equalTo(orderStatus);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // Nếu không có đơn hàng nào, hiển thị thông báo và dừng lại
                    progressBar.setVisibility(View.GONE);
                    textViewNoOrders.setVisibility(View.VISIBLE);
                    orderList.clear();
                    adapter.notifyDataSetChanged();
                    return;
                }

                final long totalOrders = dataSnapshot.getChildrenCount();
                final AtomicInteger ordersProcessed = new AtomicInteger(0);
                orderList.clear();

                // Vòng lặp cho mỗi đơn hàng trong kết quả
                for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                    // 1. Lấy thông tin cơ bản của đơn hàng
                    OrderModel order = orderSnapshot.getValue(OrderModel.class);
                    String orderId = orderSnapshot.getKey();
                    if (order != null) {
                        order.setOrderId(orderId);

                        // 2. Với mỗi đơn hàng, thực hiện truy vấn thứ hai để lấy items
                        DatabaseReference itemsRef = databaseReference.child("OrderItem").child(orderId);
                        itemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot itemsSnapshot) {
                                List<OrderItemModel> items = new ArrayList<>();
                                for (DataSnapshot itemData : itemsSnapshot.getChildren()) {
                                    items.add(itemData.getValue(OrderItemModel.class));
                                }
                                // 3. Gán danh sách items vào đối tượng order
                                order.setItems(items);
                                orderList.add(order);

                                // 4. Kiểm tra xem đã xử lý xong tất cả các đơn hàng chưa
                                if (ordersProcessed.incrementAndGet() == totalOrders) {
                                    // Chỉ cập nhật adapter và UI sau khi đã lấy đủ dữ liệu cho TẤT CẢ đơn hàng
                                    progressBar.setVisibility(View.GONE);
                                    adapter.notifyDataSetChanged();
                                    if(orderList.isEmpty()){
                                        textViewNoOrders.setVisibility(View.VISIBLE);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // Xử lý lỗi khi lấy item, và vẫn kiểm tra để hoàn thành
                                if (ordersProcessed.incrementAndGet() == totalOrders) {
                                    progressBar.setVisibility(View.GONE);
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Lỗi tải danh sách đơn hàng.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
