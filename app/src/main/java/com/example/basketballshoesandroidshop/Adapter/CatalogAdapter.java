package com.example.basketballshoesandroidshop.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.example.basketballshoesandroidshop.Activity.DetailActivity;
import com.example.basketballshoesandroidshop.Domain.ItemsModel;
import com.example.basketballshoesandroidshop.R;
import com.example.basketballshoesandroidshop.Repository.MainRepository;
import com.example.basketballshoesandroidshop.Utils.WishlistCache;
import com.example.basketballshoesandroidshop.ViewModel.WishlistViewModel;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CatalogAdapter extends ArrayAdapter<ItemsModel> {

    private Context context;
    private static final String TAG = "CatalogAdapter";
    private List<ItemsModel> dataList;
    private WishlistViewModel wishlistViewModel;
    private String currentUserId;
    private WishlistCache wishlistCache;

    public CatalogAdapter(@NonNull Context context, int resource, @NonNull List<ItemsModel> objects, String userId) {
        super(context, resource, objects);
        this.context = context;
        this.dataList = objects;
        this.currentUserId = userId;
        this.wishlistViewModel = new WishlistViewModel();
        this.wishlistCache = WishlistCache.getInstance();
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.tvName = convertView.findViewById(R.id.tv_product_name);
            viewHolder.tvDescription = convertView.findViewById(R.id.tv_product_description);
            viewHolder.imvProduct = convertView.findViewById(R.id.imv_product_img);
            viewHolder.imvLoveLike = convertView.findViewById(R.id.iv_love_like);
            viewHolder.tvPrice = convertView.findViewById(R.id.text_price);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final ItemsModel item = dataList.get(position);
        if (item == null) {
            return convertView;
        }
        viewHolder.tvName.setText(item.getTitle());
        viewHolder.tvDescription.setText(item.getDescription());
        viewHolder.tvPrice.setText("$" + item.getPrice());
        
        if (item.getPicUrl() != null && !item.getPicUrl().isEmpty()) {
            Picasso.with(context)
                    .load(item.getPicUrl().get(0))
                    .into(viewHolder.imvProduct);
        }

        // Update local state from cache (instant, no async call needed)
        viewHolder.currentItemId = item.getId();
        if (item.getId() != null) {
            viewHolder.isInWishlist = wishlistCache.isInWishlist(item.getId());
            updateHeartIcon(viewHolder, viewHolder.isInWishlist);
            
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
                    if (item.getId().equals(viewHolder.currentItemId)) {
                        viewHolder.isInWishlist = isInWishlist;
                        updateHeartIcon(viewHolder, isInWishlist);
                    }
                }
            });
        }

        // Wishlist button click listener
        viewHolder.imvLoveLike.setOnClickListener(v -> {
            if (item.getId() != null) {
                // Toggle UI and cache immediately for instant feedback
                viewHolder.isInWishlist = !viewHolder.isInWishlist;
                updateHeartIcon(viewHolder, viewHolder.isInWishlist);
                
                if (viewHolder.isInWishlist) {
                    // Add to cache and wishlist
                    wishlistCache.addToWishlist(item.getId());
                    wishlistViewModel.addToWishlist(currentUserId, item.getId())
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "Đã thêm vào danh sách yêu thích", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                // Revert UI and cache on failure
                                viewHolder.isInWishlist = false;
                                wishlistCache.removeFromWishlist(item.getId());
                                updateHeartIcon(viewHolder, false);
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
                                viewHolder.isInWishlist = true;
                                wishlistCache.addToWishlist(item.getId());
                                updateHeartIcon(viewHolder, true);
                                Toast.makeText(context, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                            });
                }
            }
        });
        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("object", item); // đảm bảo ItemsModel implements Serializable
            context.startActivity(intent);
        });


        return convertView;
    }
    
    private void updateHeartIcon(ViewHolder viewHolder, boolean isInWishlist) {
        if (isInWishlist) {
            viewHolder.imvLoveLike.setImageTintList(ContextCompat.getColorStateList(context, R.color.red));
        } else {
            viewHolder.imvLoveLike.setImageTintList(ContextCompat.getColorStateList(context, R.color.darkGrey));
        }
    }

    private static class ViewHolder {
        TextView tvName, tvPrice;
        TextView tvDescription;
        ImageView imvProduct;
        ImageButton imvLoveLike;
        boolean isInWishlist; // Local state tracking
        String currentItemId; // Track current item ID
    }
}
