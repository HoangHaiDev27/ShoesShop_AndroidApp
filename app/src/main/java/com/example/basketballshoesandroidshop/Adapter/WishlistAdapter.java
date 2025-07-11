package com.example.basketballshoesandroidshop.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.request.RequestOptions;
import com.example.basketballshoesandroidshop.Domain.ItemsModel;
import com.example.basketballshoesandroidshop.R;
import com.example.basketballshoesandroidshop.databinding.ViewholderWishlistBinding;

import java.util.ArrayList;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder> {
    private ArrayList<ItemsModel> items;
    private Context context;
    private OnWishlistItemClickListener listener;

    public interface OnWishlistItemClickListener {
        void onItemClick(ItemsModel item);
        void onRemoveFromWishlist(ItemsModel item);
    }

    public WishlistAdapter(ArrayList<ItemsModel> items, OnWishlistItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WishlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderWishlistBinding binding = 
                ViewholderWishlistBinding.inflate(LayoutInflater.from(context), parent, false);
        return new WishlistViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ItemsModel item = items.get(position);
        
        holder.binding.titleTxt.setText(item.getTitle());
        holder.binding.priceTxt.setText("$" + item.getPrice());
        holder.binding.ratingTxt.setText("(" + item.getRating() + ")");
        
        if (item.getOffPercent() != null && !item.getOffPercent().isEmpty()) {
            holder.binding.offPercentTxt.setText(item.getOffPercent() + " Off");
            holder.binding.offPercentTxt.setVisibility(View.VISIBLE);
            holder.binding.oldPriceTxt.setText("$" + item.getOldPrice());
            holder.binding.oldPriceTxt.setPaintFlags(holder.binding.oldPriceTxt.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.binding.oldPriceTxt.setVisibility(View.VISIBLE);
        } else {
            holder.binding.offPercentTxt.setVisibility(View.GONE);
            holder.binding.oldPriceTxt.setVisibility(View.GONE);
        }

        RequestOptions options = new RequestOptions();
        options = options.transform(new CenterInside());

        if (item.getPicUrl() != null && !item.getPicUrl().isEmpty()) {
            Glide.with(context)
                    .load(item.getPicUrl().get(0))
                    .apply(options)
                    .into(holder.binding.pic);
        }

        // Item click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });

        // Remove from wishlist button
        holder.binding.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveFromWishlist(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateItems(ArrayList<ItemsModel> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public class WishlistViewHolder extends RecyclerView.ViewHolder {
        ViewholderWishlistBinding binding;
        
        public WishlistViewHolder(ViewholderWishlistBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
} 