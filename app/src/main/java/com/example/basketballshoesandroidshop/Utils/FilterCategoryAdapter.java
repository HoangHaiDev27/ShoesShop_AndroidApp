package com.example.basketballshoesandroidshop.Utils;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.basketballshoesandroidshop.Domain.CategoryModel;
import com.example.basketballshoesandroidshop.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FilterCategoryAdapter extends RecyclerView.Adapter<FilterCategoryAdapter.ViewHolder> {
    
    private List<CategoryModel> allCategories;
    private List<CategoryModel> selectedCategories;
    private OnFilterChangeListener listener;
    
    public interface OnFilterChangeListener {
        void onFilterChanged();
    }
    
    public FilterCategoryAdapter(List<CategoryModel> allCategories, List<CategoryModel> selectedCategories, OnFilterChangeListener listener) {
        this.allCategories = allCategories;
        this.selectedCategories = selectedCategories;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryModel category = allCategories.get(position);
        
        holder.categoryName.setText(category.getTitle());
        
        // Kiểm tra xem category có được chọn không
        boolean isSelected = isItemSelected(category);
        updateSelectionState(holder, isSelected);
        
        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (isItemSelected(category)) {
                // Bỏ chọn
                removeFromSelected(category);
            } else {
                // Chọn
                selectedCategories.add(category);
            }
            notifyItemChanged(position);
            if (listener != null) {
                listener.onFilterChanged();
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return allCategories.size();
    }
    
    private boolean isItemSelected(CategoryModel category) {
        for (CategoryModel selected : selectedCategories) {
            if (selected.getId() == category.getId()) {
                return true;
            }
        }
        return false;
    }
    
    private void removeFromSelected(CategoryModel category) {
        selectedCategories.removeIf(selected -> selected.getId() == category.getId());
    }
    
    private void updateSelectionState(ViewHolder holder, boolean isSelected) {
        if (isSelected) {
            holder.indicator.setCardBackgroundColor(Color.parseColor("#FF6B35"));
            holder.indicator.setVisibility(View.VISIBLE);
        } else {
            holder.indicator.setCardBackgroundColor(Color.TRANSPARENT);
            holder.indicator.setVisibility(View.GONE);
        }
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView categoryImage;
        TextView categoryName;
        CardView indicator;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryImage = itemView.findViewById(R.id.img_category_item);
            categoryName = itemView.findViewById(R.id.tv_category_name);
            indicator = itemView.findViewById(R.id.indicator);
        }
    }
} 