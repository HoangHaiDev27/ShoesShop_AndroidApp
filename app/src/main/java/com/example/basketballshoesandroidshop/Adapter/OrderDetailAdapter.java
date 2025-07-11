package com.example.basketballshoesandroidshop.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.basketballshoesandroidshop.Domain.OrderItemModel;
import com.example.basketballshoesandroidshop.R;

import java.util.List;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.ViewHolder> {
    private List<OrderItemModel> items;
    private Context context;

    public OrderDetailAdapter(List<OrderItemModel> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_detail_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderItemModel item = items.get(position);
        holder.tvProductTitle.setText(item.getTitle());
        holder.tvProductQuantity.setText("Số lượng: " + item.getQuantity());
        holder.tvProductPrice.setText("Giá: " + item.getPrice() + "đ"); // Định dạng lại nếu cần

        Glide.with(context)
                .load(item.getPicUrl())
                .into(holder.ivProductImage);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductTitle, tvProductQuantity, tvProductPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductTitle = itemView.findViewById(R.id.tvProductTitle);
            tvProductQuantity = itemView.findViewById(R.id.tvProductQuantity);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
        }
    }
}
