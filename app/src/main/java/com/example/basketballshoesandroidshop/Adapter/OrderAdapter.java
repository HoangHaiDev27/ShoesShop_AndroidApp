package com.example.basketballshoesandroidshop.Adapter;

import android.content.Context;
import android.content.Intent;
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


     /*   if ("Đã giao".equals(order.getOrderStatus())) {
            holder.textViewOrderStatus.setText("Hoàn thành");
            holder.buttonRate.setVisibility(View.VISIBLE);
        } else {
            holder.textViewOrderStatus.setText(order.getOrderStatus());
            holder.buttonRate.setVisibility(View.GONE);
        }*/

        if ("Đã giao".equalsIgnoreCase(order.getOrderStatus()) || "Hoàn thành".equalsIgnoreCase(order.getOrderStatus())) {
            holder.buttonRate.setVisibility(View.VISIBLE);
        } else {
            holder.buttonRate.setVisibility(View.GONE);
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
