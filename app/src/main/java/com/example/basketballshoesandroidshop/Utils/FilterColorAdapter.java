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

import com.example.basketballshoesandroidshop.Domain.VariationModel;
import com.example.basketballshoesandroidshop.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterColorAdapter extends RecyclerView.Adapter<FilterColorAdapter.ViewHolder> {
    
    private List<VariationModel> allColors;
    private List<VariationModel> selectedColors;
    private OnFilterChangeListener listener;
    
    // Map màu sắc với mã màu hex
    private Map<String, String> colorMap;
    
    public interface OnFilterChangeListener {
        void onFilterChanged();
    }
    
    public FilterColorAdapter(List<VariationModel> allColors, List<VariationModel> selectedColors, OnFilterChangeListener listener) {
        this.allColors = allColors;
        this.selectedColors = selectedColors;
        this.listener = listener;
        initColorMap();
    }
    
    private void initColorMap() {
        colorMap = new HashMap<>();
        colorMap.put("black", "#000000");
        colorMap.put("white", "#FFFFFF");
        colorMap.put("red", "#FF0000");
        colorMap.put("blue", "#0000FF");
        colorMap.put("green", "#00FF00");
        colorMap.put("yellow", "#FFFF00");
        colorMap.put("purple", "#800080");
        colorMap.put("orange", "#FFA500");
        colorMap.put("pink", "#FFC0CB");
        colorMap.put("brown", "#A52A2A");
        colorMap.put("gray", "#808080");
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VariationModel color = allColors.get(position);
        
        holder.colorName.setText(capitalizeFirstLetter(color.name));
        
        // Thiết lập màu cho ImageView
        String colorHex = colorMap.get(color.name.toLowerCase());
        if (colorHex != null) {
            try {
                int colorInt = Color.parseColor(colorHex);
                holder.colorImage.setBackgroundColor(colorInt);
                
                // Thêm border cho màu trắng để dễ nhìn
                if (color.name.toLowerCase().equals("white")) {
                    holder.colorImage.setBackgroundResource(R.drawable.stroke_bg);
                }
            } catch (Exception e) {
                holder.colorImage.setBackgroundColor(Color.GRAY);
            }
        } else {
            holder.colorImage.setBackgroundColor(Color.GRAY);
        }
        
        // Kiểm tra xem color có được chọn không
        boolean isSelected = isItemSelected(color);
        updateSelectionState(holder, isSelected);
        
        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (isItemSelected(color)) {
                // Bỏ chọn
                removeFromSelected(color);
            } else {
                // Chọn
                selectedColors.add(color);
            }
            notifyItemChanged(position);
            if (listener != null) {
                listener.onFilterChanged();
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return allColors.size();
    }
    
    private boolean isItemSelected(VariationModel color) {
        for (VariationModel selected : selectedColors) {
            if (selected.id.equals(color.id)) {
                return true;
            }
        }
        return false;
    }
    
    private void removeFromSelected(VariationModel color) {
        selectedColors.removeIf(selected -> selected.id.equals(color.id));
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
    
    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView colorImage;
        TextView colorName;
        CardView indicator;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            colorImage = itemView.findViewById(R.id.img_category_item);
            colorName = itemView.findViewById(R.id.tv_category_name);
            indicator = itemView.findViewById(R.id.indicator);
        }
    }
} 