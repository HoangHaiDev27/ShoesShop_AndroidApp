package com.example.basketballshoesandroidshop.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.request.RequestOptions;
import com.example.basketballshoesandroidshop.Activity.DetailActivity;
import com.example.basketballshoesandroidshop.Domain.ItemsModel;
import com.example.basketballshoesandroidshop.R;
import com.example.basketballshoesandroidshop.Repository.MainRepository;
import com.example.basketballshoesandroidshop.Utils.WishlistCache;
import com.example.basketballshoesandroidshop.ViewModel.WishlistViewModel;
import com.example.basketballshoesandroidshop.databinding.ViewholderPopularBinding;

import java.util.ArrayList;

public class PopularAdapter extends RecyclerView.Adapter<PopularAdapter.PopularViewHolder>{
    private ArrayList<ItemsModel> items;
    private Context context;
    private WishlistViewModel wishlistViewModel;
    private String currentUserId;
    private WishlistCache wishlistCache;

    public PopularAdapter(ArrayList<ItemsModel> items, String userId) {
        this.items = items;
        this.currentUserId = userId;
        this.wishlistViewModel = new WishlistViewModel();
        this.wishlistCache = WishlistCache.getInstance();
    }

    @NonNull
    @Override
    public PopularAdapter.PopularViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderPopularBinding binding =
                ViewholderPopularBinding.inflate(LayoutInflater.from(context),parent,false);
        return new PopularViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PopularAdapter.PopularViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ItemsModel item = items.get(position);
        
        holder.binding.titleTxt.setText(item.getTitle());
        holder.binding.priceTxt.setText("$" + item.getPrice());
        holder.binding.ratingTxt.setText("(" + item.getRating() + ")");
        holder.binding.offPercentTxt.setText(item.getOffPercent() + " Off");
        holder.binding.oldPriceTxt.setText("$" + item.getOldPrice());
        holder.binding.oldPriceTxt.setPaintFlags(holder.binding.oldPriceTxt.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        RequestOptions options = new RequestOptions();
        options = options.transform(new CenterInside());

        if (item.getPicUrl() != null && !item.getPicUrl().isEmpty()) {
            Glide.with(context)
                    .load(item.getPicUrl().get(0))
                    .apply(options)
                    .into(holder.binding.pic);
        }

        // Update local state from cache (instant, no async call needed)
        holder.currentItemId = item.getId();
        if (item.getId() != null) {
            holder.isInWishlist = wishlistCache.isInWishlist(item.getId());
            updateHeartIcon(holder, holder.isInWishlist);
            
            // Sync with Firebase in background (để đảm bảo cache đúng)
            wishlistViewModel.checkWishlistStatus(currentUserId, item.getId(), new MainRepository.WishlistStatusCallback() {
                @Override
                public void onResult(boolean isInWishlist) {
                    // Update cache
                    if (isInWishlist) {
                        wishlistCache.addToWishlist(item.getId());
                    } else {
                        wishlistCache.removeFromWishlist(item.getId());
                    }
                    
                    // Update UI if current item
                    if (item.getId().equals(holder.currentItemId)) {
                        holder.isInWishlist = isInWishlist;
                        updateHeartIcon(holder, isInWishlist);
                    }
                }
            });
        }

        // Item click listener
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("object", item);
            context.startActivity(intent);
        });

        // Wishlist button click listener
        holder.binding.btnWishlist.setOnClickListener(v -> {
            if (item.getId() != null) {
                // Toggle UI and cache immediately for instant feedback
                holder.isInWishlist = !holder.isInWishlist;
                updateHeartIcon(holder, holder.isInWishlist);
                
                if (holder.isInWishlist) {
                    // Add to cache and wishlist
                    wishlistCache.addToWishlist(item.getId());
                    wishlistViewModel.addToWishlist(currentUserId, item.getId())
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "Đã thêm vào danh sách yêu thích", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                // Revert UI and cache on failure
                                holder.isInWishlist = false;
                                wishlistCache.removeFromWishlist(item.getId());
                                updateHeartIcon(holder, false);
                                Toast.makeText(context, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    // Remove from cache and wishlist
                    wishlistCache.removeFromWishlist(item.getId());
                    wishlistViewModel.removeFromWishlist(currentUserId, item.getId())
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "Đã xóa khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                // Revert UI and cache on failure
                                holder.isInWishlist = true;
                                wishlistCache.addToWishlist(item.getId());
                                updateHeartIcon(holder, true);
                                Toast.makeText(context, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                            });
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
    
    private void updateHeartIcon(PopularViewHolder holder, boolean isInWishlist) {
        if (isInWishlist) {
            holder.binding.btnWishlist.setImageTintList(ContextCompat.getColorStateList(context, R.color.red));
        } else {
            holder.binding.btnWishlist.setImageTintList(ContextCompat.getColorStateList(context, R.color.darkGrey));
        }
    }

    public class PopularViewHolder extends RecyclerView.ViewHolder {
        ViewholderPopularBinding binding;
        boolean isInWishlist; // Local state tracking
        String currentItemId; // Track current item ID
        
        public PopularViewHolder(ViewholderPopularBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
