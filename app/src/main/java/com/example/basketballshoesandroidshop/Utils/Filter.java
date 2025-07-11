package com.example.basketballshoesandroidshop.Utils;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.basketballshoesandroidshop.Domain.VariationModel;
import com.example.basketballshoesandroidshop.R;
import com.example.basketballshoesandroidshop.Utils.CurrencyFormat;
import com.example.basketballshoesandroidshop.ViewModel.FilterViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.RangeSlider;

import java.util.ArrayList;
import java.util.List;

public class Filter extends AppCompatActivity {
    
    private FilterViewModel filterViewModel;
    private RangeSlider priceRangeSlider;
    private TextView minPriceText, maxPriceText, showItemsButton;
    private RecyclerView colorsRecyclerView;
    private ImageButton closeButton;
    private TextView clearButton;
    private MaterialButton showMoreButton;
    
    private FilterColorAdapter colorAdapter;
    
    private List<VariationModel> allColors = new ArrayList<>();
    private List<VariationModel> selectedColors = new ArrayList<>();
    
    private double minPrice = 0;
    private double maxPrice = 100;
    private int categoryId = 0;
    private String searchContent = "";
    
    // Filter state từ Intent để khôi phục
    private double currentMinPrice = 0;
    private double currentMaxPrice = 100;
    private List<VariationModel> currentSelectedColors = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            android.util.Log.d("FilterActivity", "onCreate started");
            
            setContentView(R.layout.activity_filter);
            android.util.Log.d("FilterActivity", "Layout set successfully");
            
            // Nhận data từ Intent
            Intent intent = getIntent();
            if (intent != null) {
                categoryId = intent.getIntExtra("categoryId", 0);
                searchContent = intent.getStringExtra("searchContent");
                if (searchContent == null) searchContent = "";
                
                // Nhận filter state hiện tại để khôi phục
                currentMinPrice = intent.getDoubleExtra("currentMinPrice", 0);
                currentMaxPrice = intent.getDoubleExtra("currentMaxPrice", 100);
                currentSelectedColors = (List<VariationModel>) intent.getSerializableExtra("currentSelectedColors");
                if (currentSelectedColors == null) currentSelectedColors = new ArrayList<>();
                
                android.util.Log.d("FilterActivity", "Intent data received: categoryId=" + categoryId + ", searchContent=" + searchContent);
                android.util.Log.d("FilterActivity", "Filter state received: minPrice=" + currentMinPrice + ", maxPrice=" + currentMaxPrice + ", colors=" + currentSelectedColors.size());
            }
            
            initViews();
            android.util.Log.d("FilterActivity", "Views initialized");
            
            setupViewModels();
            android.util.Log.d("FilterActivity", "ViewModels setup");
            
            setupSlider();
            android.util.Log.d("FilterActivity", "Slider setup");
            
            setupRecyclerViews();
            android.util.Log.d("FilterActivity", "RecyclerViews setup");
            
            setupClickListeners();
            android.util.Log.d("FilterActivity", "Click listeners setup");
            
            loadData();
            android.util.Log.d("FilterActivity", "Data loading initiated");
            
            android.util.Log.d("FilterActivity", "onCreate completed successfully");
        } catch (Exception e) {
            android.util.Log.e("FilterActivity", "Error in onCreate: " + e.getMessage(), e);
            throw e; // Re-throw to see the actual error
        }
    }
    
    private void initViews() {
        try {
            android.util.Log.d("FilterActivity", "Finding priceRangeSlider");
            priceRangeSlider = findViewById(R.id.filter_rs_price);
            
            android.util.Log.d("FilterActivity", "Finding price text views");
            minPriceText = findViewById(R.id.filter_tv_minPrice);
            maxPriceText = findViewById(R.id.filter_tv_maxPrice);
            
            android.util.Log.d("FilterActivity", "Finding colors recycler view");
            colorsRecyclerView = findViewById(R.id.filter_lv_listColours);
            
            android.util.Log.d("FilterActivity", "Finding buttons");
            closeButton = findViewById(R.id.filter_ib_close);
            clearButton = findViewById(R.id.filter_tv_clear);
            showMoreButton = findViewById(R.id.filter_bt_showMore);
            
            android.util.Log.d("FilterActivity", "All views found successfully");
        } catch (Exception e) {
            android.util.Log.e("FilterActivity", "Error in initViews: " + e.getMessage(), e);
            throw e;
        }
    }
    
    private void setupViewModels() {
        filterViewModel = new ViewModelProvider(this).get(FilterViewModel.class);
    }
    
    private void setupSlider() {
        priceRangeSlider.setValueFrom(0f);
        priceRangeSlider.setValueTo(100f);
        
        // Khôi phục giá trị từ Intent hoặc sử dụng default
        minPrice = currentMinPrice;
        maxPrice = currentMaxPrice;
        
        priceRangeSlider.setValues((float) minPrice, (float) maxPrice);
        minPriceText.setText(CurrencyFormat.format(minPrice));
        maxPriceText.setText(CurrencyFormat.format(maxPrice));
        
        android.util.Log.d("FilterActivity", "Slider setup with values: min=" + minPrice + ", max=" + maxPrice);
        
        priceRangeSlider.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            minPrice = values.get(0);
            maxPrice = values.get(1);
            
            minPriceText.setText(CurrencyFormat.format(minPrice));
            maxPriceText.setText(CurrencyFormat.format(maxPrice));
            
            updateItemCount();
        });
    }
    
    private void setupRecyclerViews() {
        // Setup Colors RecyclerView only
        colorsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        colorAdapter = new FilterColorAdapter(allColors, selectedColors, this::updateItemCount);
        colorsRecyclerView.setAdapter(colorAdapter);
    }
    
    private void setupClickListeners() {
        closeButton.setOnClickListener(v -> finish());
        
        clearButton.setOnClickListener(v -> {
            clearAllFilters();
        });
        
        showMoreButton.setOnClickListener(v -> {
            // Trả kết quả về CatalogActivity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("minPrice", minPrice);
            resultIntent.putExtra("maxPrice", maxPrice);
            resultIntent.putExtra("selectedColors", new ArrayList<>(selectedColors));
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }
    
    private void loadData() {
        // Load colors (tạo dữ liệu màu từ danh sách có sẵn)
        loadColors();
        
        // Khôi phục selected colors từ Intent
        restoreSelectedColors();
        
        updateItemCount();
        
        // Initialize clear button state
        updateClearButtonVisibility();
    }
    
    private void loadColors() {
        // Tạo danh sách màu cơ bản
        String[] colorNames = {"black", "white", "red", "blue", "green", "yellow", "purple", "orange", "pink", "brown", "gray"};
        
        allColors.clear();
        for (int i = 0; i < colorNames.length; i++) {
            VariationModel color = new VariationModel();
            color.id = String.valueOf(i);
            color.name = colorNames[i];
            color.inventory = 1; // Available
            allColors.add(color);
        }
        colorAdapter.notifyDataSetChanged();
    }
    
    private void updateItemCount() {
        // Đây là nơi bạn có thể tính toán số lượng items phù hợp với filter
        // Tạm thời hiển thị text tĩnh, có thể tích hợp với database để đếm thực tế
        showMoreButton.setText("Hiển thị sản phẩm");
        
        // Update clear button visibility based on active filters
        updateClearButtonVisibility();
    }
    
    /**
     * Cập nhật trạng thái hiển thị của nút Clear
     */
    private void updateClearButtonVisibility() {
        if (hasActiveFilters()) {
            clearButton.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            clearButton.setText("Clear");
        } else {
            clearButton.setTextColor(getResources().getColor(android.R.color.darker_gray));
            clearButton.setText("Clear");
        }
    }
    
    /**
     * Clear tất cả các filter đã chọn và reset về trạng thái ban đầu
     */
    private void clearAllFilters() {
        try {
            android.util.Log.d("FilterActivity", "Clearing all filters");
            
            // Kiểm tra xem có filter nào đang active không
            boolean hadActiveFilters = hasActiveFilters();
            
            if (!hadActiveFilters) {
                Toast.makeText(this, "Không có filter nào để xóa", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Reset price range slider về giá trị ban đầu
            priceRangeSlider.setValues(0f, 100f);
            minPrice = 0;
            maxPrice = 100;
            
            // Update price text display
            minPriceText.setText(CurrencyFormat.format(minPrice));
            maxPriceText.setText(CurrencyFormat.format(maxPrice));
            
            // Clear selected colors
            selectedColors.clear();
            
            // Notify adapter to refresh UI
            if (colorAdapter != null) {
                colorAdapter.notifyDataSetChanged();
                android.util.Log.d("FilterActivity", "Color adapter updated");
            }
            
            // Update item count display
            updateItemCount();
            
            // Show success message
            Toast.makeText(this, "Đã xóa tất cả filter", Toast.LENGTH_SHORT).show();
            
            android.util.Log.d("FilterActivity", "All filters cleared successfully");
            
        } catch (Exception e) {
            android.util.Log.e("FilterActivity", "Error clearing filters: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khi xóa filter", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Khôi phục selected colors từ Intent
     */
    private void restoreSelectedColors() {
        if (currentSelectedColors != null && !currentSelectedColors.isEmpty()) {
            selectedColors.clear();
            for (VariationModel currentColor : currentSelectedColors) {
                // Tìm color tương ứng trong allColors và add vào selectedColors
                for (VariationModel color : allColors) {
                    if (color.id.equals(currentColor.id) || color.name.equals(currentColor.name)) {
                        selectedColors.add(color);
                        break;
                    }
                }
            }
            
            if (colorAdapter != null) {
                colorAdapter.notifyDataSetChanged();
            }
            
            android.util.Log.d("FilterActivity", "Restored " + selectedColors.size() + " selected colors");
        }
    }
    
    /**
     * Kiểm tra xem có filter nào đang được áp dụng không
     */
    private boolean hasActiveFilters() {
        return minPrice > 0 || maxPrice < 100 || !selectedColors.isEmpty();
    }
    
    /**
     * Lấy thông tin về các filter đang được áp dụng
     */
    private String getActiveFiltersInfo() {
        StringBuilder info = new StringBuilder();
        
        if (minPrice > 0 || maxPrice < 100) {
            info.append("Giá: ").append(CurrencyFormat.format(minPrice))
                .append(" - ").append(CurrencyFormat.format(maxPrice));
        }
        
        if (!selectedColors.isEmpty()) {
            if (info.length() > 0) info.append(", ");
            info.append("Màu: ").append(selectedColors.size()).append(" màu đã chọn");
        }
        
        return info.toString();
    }
    
    /**
     * Reset filter về trạng thái mặc định khi khởi tạo
     */
    private void resetToDefault() {
        minPrice = 0;
        maxPrice = 100;
        selectedColors.clear();
        
        if (priceRangeSlider != null) {
            priceRangeSlider.setValues(0f, 100f);
        }
        
        if (minPriceText != null && maxPriceText != null) {
            minPriceText.setText(CurrencyFormat.format(minPrice));
            maxPriceText.setText(CurrencyFormat.format(maxPrice));
        }
        
        if (colorAdapter != null) {
            colorAdapter.notifyDataSetChanged();
        }
        
        updateItemCount();
    }
}
