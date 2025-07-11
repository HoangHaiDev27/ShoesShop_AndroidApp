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

    // Viết lại hoàn toàn phương thức fetchOrders
    private void fetchOrders() {
        if (orderStatus == null) {
            Log.e("FetchOrders", "orderStatus bị null! Không thể thực hiện truy vấn.");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        textViewNoOrders.setVisibility(View.GONE);

        String currentUserId = "user_001"; // Giả sử userId


        Query query = databaseReference.child("Orders").child(currentUserId)
                .orderByChild("orderStatus")
                .equalTo(orderStatus);

        // Sử dụng addListenerForSingleValueEvent để lấy dữ liệu một lần
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                orderList.clear();

                // Duyệt qua kết quả
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Chuyển đổi dữ liệu thành OrderModel
                    OrderModel order = snapshot.getValue(OrderModel.class);
                    if (order != null) {
                        // Lấy key của đơn hàng (ví dụ: "order_001")
                        order.setOrderId(snapshot.getKey());
                        orderList.add(order);
                    }
                }

                adapter.notifyDataSetChanged();

                if (orderList.isEmpty()) {
                    textViewNoOrders.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Lỗi khi tải đơn hàng: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("FetchOrders", "Lỗi Realtime Database: ", databaseError.toException());
            }
        });
    }
}
