package com.example.basketballshoesandroidshop.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.basketballshoesandroidshop.Activity.OrderDetailActivity;
import com.example.basketballshoesandroidshop.Activity.RatingDialogFragment;
import com.example.basketballshoesandroidshop.Domain.OrderItemModel;
import com.example.basketballshoesandroidshop.Domain.OrderModel;
import com.example.basketballshoesandroidshop.R;
import com.example.basketballshoesandroidshop.Utils.SessionManager;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import com.example.basketballshoesandroidshop.Repository.MainRepository;
import android.util.Log;
import com.example.basketballshoesandroidshop.Activity.CartAcitivity;

import java.util.concurrent.atomic.AtomicInteger;
import androidx.appcompat.app.AlertDialog;
import com.example.basketballshoesandroidshop.Domain.CartItemModel;
// Thêm import cho tính năng mua lại
import java.util.ArrayList;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final List<OrderModel> orderList;
    private final Context context;
    private final LayoutInflater inflater;

    public OrderAdapter(List<OrderModel> orderList, Context context) {
        this.orderList = orderList;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderModel order = orderList.get(position);

        holder.textViewStoreName.setText("Store");

        if ("Đã giao".equalsIgnoreCase(order.getOrderStatus()) || "Hoàn thành".equalsIgnoreCase(order.getOrderStatus())) {
            holder.buttonRate.setVisibility(View.VISIBLE);
        } else {
            holder.buttonRate.setVisibility(View.GONE);
        }

        // THÊM LOGIC MUA LẠI - CHỈ HIỂN THỊ CHO ĐƠN HÀNG ĐÃ GIAO
        if ("Đã giao".equals(order.getOrderStatus())) {
            holder.buttonBuyAgain.setVisibility(View.VISIBLE);
            holder.buttonBuyAgain.setOnClickListener(v -> handleRebuyOrder(order));
        } else {
            holder.buttonBuyAgain.setVisibility(View.GONE);
        }

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.textViewTotal.setText("Tổng số tiền: " + currencyFormat.format(order.getTotalPrice()));

        // Xóa các view item cũ trước khi thêm mới
        holder.layoutOrderItems.removeAllViews();

        // Dynamically add product items
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItemModel item : order.getItems()) {
                View itemView = inflater.inflate(R.layout.item_order_product, holder.layoutOrderItems, false);

                ImageView imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
                TextView textViewProductTitle = itemView.findViewById(R.id.textViewProductTitle);
                TextView textViewProductPrice = itemView.findViewById(R.id.textViewProductPrice);
                TextView textViewProductQuantity = itemView.findViewById(R.id.textViewProductQuantity);

                textViewProductTitle.setText(item.getTitle());
                textViewProductPrice.setText(currencyFormat.format(item.getPrice()));
                textViewProductQuantity.setText("x" + item.getQuantity());

                Glide.with(context)
                        .load(item.getPicUrl())
                        .placeholder(R.drawable.ic_image_placeholder) // Placeholder image
                        .into(imageViewProduct);

                holder.layoutOrderItems.addView(itemView);
            }
        }

        holder.itemView.setOnClickListener(v -> {

            String orderId = order.getOrderId();
            SessionManager sessionManager = new SessionManager(context);

            String userId = sessionManager.getCurrentUserId();

            // Tạo Intent để mở OrderDetailActivity
            Intent intent = new Intent(context, OrderDetailActivity.class);

            // Đính kèm dữ liệu cần thiết
            intent.putExtra("USER_ID", userId);
            intent.putExtra("ORDER_ID", orderId);

            // Khởi chạy Activity mới
            context.startActivity(intent);
        });

        holder.buttonRate.setOnClickListener(v -> {
            // Giả sử chúng ta đánh giá sản phẩm đầu tiên trong đơn hàng
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                String itemIdToRate = order.getItems().get(0).getItemId();
                String orderId = order.getOrderId();

                FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();

                RatingDialogFragment dialog = RatingDialogFragment.newInstance(orderId, itemIdToRate);
                dialog.show(fragmentManager, "RatingDialog");
            } else {
                Toast.makeText(context, "Không có sản phẩm để đánh giá.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // THÊM METHOD MUA LẠI
    private void handleRebuyOrder(OrderModel order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            Toast.makeText(context, "Đơn hàng không có sản phẩm để mua lại", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("🔄 Đang chuẩn bị đơn hàng")
                .setMessage("Đang thêm sản phẩm vào giỏ hàng...")
                .setCancelable(false);

        AlertDialog loadingDialog = builder.create();
        loadingDialog.show();

        // Add all items to cart
        addOrderItemsToCart(order, loadingDialog);
    }
    private void addOrderItemsToCart(OrderModel order, AlertDialog loadingDialog) {
        String userId = getCurrentUserId();
        List<OrderItemModel> orderItems = order.getItems();

        // Counter để track số items đã thêm
        AtomicInteger itemsProcessed = new AtomicInteger(0);
        AtomicInteger itemsAdded = new AtomicInteger(0);
        int totalItems = orderItems.size();

        for (OrderItemModel orderItem : orderItems) {
            // Chuyển OrderItemModel thành CartItemModel
            CartItemModel cartItem = new CartItemModel(
                    orderItem.getItemId(),
                    orderItem.getPrice(),
                    orderItem.getQuantity(),
                    orderItem.getSize(),
                    "" // Color - có thể để trống hoặc default
            );

            // Add to Firebase cart
            MainRepository repository = new MainRepository();
            repository.addToCart(userId, cartItem)
                    .addOnSuccessListener(aVoid -> {
                        itemsAdded.incrementAndGet();
                        checkAndFinalize(itemsProcessed.incrementAndGet(), totalItems,
                                itemsAdded.get(), order, loadingDialog);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("OrderAdapter", "Failed to add item to cart: " + e.getMessage());
                        checkAndFinalize(itemsProcessed.incrementAndGet(), totalItems,
                                itemsAdded.get(), order, loadingDialog);
                    });
        }
    }

    private void checkAndFinalize(int processed, int total, int added, OrderModel order, AlertDialog loadingDialog) {
        if (processed == total) {
            // Đã xử lý xong tất cả items
            loadingDialog.dismiss();

            if (added > 0) {
                // Navigate to CartActivity
                Intent intent = new Intent(context, CartAcitivity.class);
                intent.putExtra("FROM_REBUY", true);
                intent.putExtra("REBUY_ORDER_ID", order.getOrderId());
                intent.putExtra("REBUY_ITEMS_COUNT", added);
                context.startActivity(intent);

                // Show success message
                Toast.makeText(context,
                        String.format("✅ Đã thêm %d sản phẩm từ đơn #%s vào giỏ hàng",
                                added, order.getOrderId()),
                        Toast.LENGTH_LONG).show();
            } else {
                // Không thêm được item nào
                Toast.makeText(context, "❌ Không thể thêm sản phẩm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // THÊM METHOD LẤY USER ID
    private String getCurrentUserId() {
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        return prefs.getString("user_id", "user_001");
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView textViewStoreName, textViewOrderStatus, textViewTotal;
        LinearLayout layoutOrderItems;
        Button buttonBuyAgain, buttonRate;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewStoreName = itemView.findViewById(R.id.textViewStoreName);
            textViewOrderStatus = itemView.findViewById(R.id.textViewOrderStatus);
            layoutOrderItems = itemView.findViewById(R.id.layoutOrderItems);
            textViewTotal = itemView.findViewById(R.id.textViewTotal);
            buttonBuyAgain = itemView.findViewById(R.id.buttonBuyAgain);
            buttonRate = itemView.findViewById(R.id.buttonRate);
        }
    }
}