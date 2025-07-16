package com.example.basketballshoesandroidshop.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.basketballshoesandroidshop.Domain.ItemsModel;
import com.example.basketballshoesandroidshop.Helper.ChangeNumberItemsListener;
import com.example.basketballshoesandroidshop.Helper.ManagmentCart;
import com.example.basketballshoesandroidshop.Repository.MainRepository;
import com.example.basketballshoesandroidshop.databinding.ViewholderCartBinding;

import java.util.ArrayList;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.Viewholder> {
    private ArrayList<ItemsModel> listItemSelected;
    private ChangeNumberItemsListener changeNumberItemsListener;
    private String userId;
    private MainRepository repository;

    public CartAdapter(ArrayList<ItemsModel> listItemSelected, ChangeNumberItemsListener changeNumberItemsListener, String userId, MainRepository repository) {
        this.listItemSelected = listItemSelected;
        this.changeNumberItemsListener = changeNumberItemsListener;
        this.userId = userId;
        this.repository = repository;
    }

    @NonNull
    @Override
    public CartAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderCartBinding binding = ViewholderCartBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new Viewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartAdapter.Viewholder holder, int position) {
        ItemsModel item = listItemSelected.get(position);

        holder.binding.titleTxt.setText(item.getTitle());
        holder.binding.feeEachitem.setText("$" + item.getPrice());
        holder.binding.totalEachItem.setText("$" + Math.round(item.getNumberInCart() * item.getPrice()));
        holder.binding.numberItemtxt.setText(String.valueOf(item.getNumberInCart()));

        Glide.with(holder.itemView.getContext())
                .load(item.getPicUrl().get(0))
                .into(holder.binding.pic);

        holder.binding.plsuCarrBtn.setOnClickListener(v -> {
            int newQuantity = item.getNumberInCart() + 1;
            item.setNumberInCart(newQuantity);

            repository.updateCartItemQuantity(userId, item.getId(), newQuantity)
                    .addOnSuccessListener(aVoid -> {
                        notifyDataSetChanged();
                        changeNumberItemsListener.changed();
                    });
        });

        holder.binding.minimusCart.setOnClickListener(v -> {
            int currentQuantity = item.getNumberInCart();
            if (currentQuantity > 1) {
                int newQuantity = currentQuantity - 1;
                item.setNumberInCart(newQuantity);

                repository.updateCartItemQuantity(userId, item.getId(), newQuantity)
                        .addOnSuccessListener(aVoid -> {
                            notifyDataSetChanged();
                            changeNumberItemsListener.changed();
                        });
            } else {
                // Xóa khỏi giỏ hàng nếu số lượng = 1
                repository.removeItemFromCart(userId, item.getId())
                        .addOnSuccessListener(aVoid -> {
                            listItemSelected.remove(position);
                            notifyDataSetChanged();
                            changeNumberItemsListener.changed();
                        });
            }
        });
    }

    @Override
    public int getItemCount() {
        return listItemSelected.size();
    }

    public static class Viewholder extends RecyclerView.ViewHolder {
        ViewholderCartBinding binding;
        public Viewholder(ViewholderCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

