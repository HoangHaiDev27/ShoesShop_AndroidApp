package com.example.basketballshoesandroidshop.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.GridView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.basketballshoesandroidshop.Adapter.CatalogAdapter;
import com.example.basketballshoesandroidshop.Domain.CategoryModel;
import com.example.basketballshoesandroidshop.Domain.ItemsModel;
import com.example.basketballshoesandroidshop.Domain.VariationModel;
import com.example.basketballshoesandroidshop.Utils.Filter;
import com.example.basketballshoesandroidshop.Utils.Sort;
import com.example.basketballshoesandroidshop.ViewModel.CatalogViewModel;
import com.example.basketballshoesandroidshop.databinding.ActivityCatalogBinding;

import java.util.ArrayList;
import java.util.List;

public class CatalogActivity extends AppCompatActivity {
    private GridView gridView;
    private CatalogAdapter adapter;
    private List<ItemsModel> dataList;
    private ActivityCatalogBinding binding;
    private int categoryId;
    private String categoryName;
    private CatalogViewModel viewModel;
    private EditText edtSearch;
    private Sort sort;
    private static final String TAG = "CatalogActivity";
    
    // Filter-related fields
    private double minPrice = 0;
    private double maxPrice = 100;
    private List<VariationModel> selectedColors = new ArrayList<>();
    private ActivityResultLauncher<Intent> filterLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCatalogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        categoryId = intent.getIntExtra("categoryId", 1);
        categoryName = intent.getStringExtra("categoryName");

        setupFilterLauncher();
        init();
        sort = new Sort(categoryId, viewModel, edtSearch.getText().toString().trim());
        loadData();
        setUpEditText();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh CatalogAdapter để cập nhật icon heart
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void setUpEditText() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed in this case
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filter the products based on the search query
                sort.setSearchContent(s.toString());
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed in this case
            }
        });
    }

    private void filterProducts(String query) {

        List<ItemsModel> originalProducts = viewModel.getAllProductsByCategory(categoryId).getValue();
        if (originalProducts != null) {

            List<ItemsModel> filteredProducts = new ArrayList<>();

            for (ItemsModel product : originalProducts) {
                if (product.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredProducts.add(product);
                }
            }

            adapter = new CatalogAdapter(this, filteredProducts.size(), filteredProducts);
            Log.e(TAG, "filterProducts: " + filteredProducts.size());
            gridView.setAdapter(adapter);
        }
    }


    private void loadData() {
        viewModel.getAllProductsByCategory(categoryId).observe(this, productModels -> {
            dataList = new ArrayList<>();
            for (ItemsModel product : productModels) {
                dataList.add(product);
            }
            adapter = new CatalogAdapter(this, dataList.size(), dataList);
            Log.e(TAG, "loadData123: " + dataList.size());
            gridView.setAdapter(adapter);
        });
    }

    public void init() {
        viewModel = new ViewModelProvider(this).get(CatalogViewModel.class);
        gridView = binding.gvProduct;
        binding.appbar.appbarTitle.setText(categoryName);
        dataList = new ArrayList<>();
        edtSearch = binding.edtSearch;

        binding.appbar.btnBack.setOnClickListener(v -> {
            finish();
        });

        binding.btnSort.setOnClickListener(v -> {
            sort.show(getSupportFragmentManager(), "Sort");
        });
        
        binding.btnFilter.setOnClickListener(v -> {
            Intent filterIntent = new Intent(this, Filter.class);
            filterIntent.putExtra("categoryId", categoryId);
            filterIntent.putExtra("searchContent", edtSearch.getText().toString().trim());
            
            // Truyền filter state hiện tại để khôi phục
            filterIntent.putExtra("currentMinPrice", minPrice);
            filterIntent.putExtra("currentMaxPrice", maxPrice);
            filterIntent.putExtra("currentSelectedColors", new ArrayList<>(selectedColors));
            
            Log.d(TAG, "Sending filter state - minPrice: " + minPrice + ", maxPrice: " + maxPrice + ", colors: " + selectedColors.size());
            
            filterLauncher.launch(filterIntent);
        });
    }
    
    private void setupFilterLauncher() {
        filterLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    
                    // Nhận filter data từ Filter activity
                    minPrice = data.getDoubleExtra("minPrice", 0);
                    maxPrice = data.getDoubleExtra("maxPrice", 100);
                    
                    selectedColors = (List<VariationModel>) data.getSerializableExtra("selectedColors");
                    
                    if (selectedColors == null) selectedColors = new ArrayList<>();
                    
                    Log.d(TAG, "Filter data received - Colors: " + selectedColors.size());
                    
                    // Áp dụng filter
                    applyFilters();
                }
            }
        );
    }
    
    private void applyFilters() {
        String searchContent = edtSearch.getText().toString().trim();
        
        // Sử dụng method có sẵn trong CatalogViewModel với filter parameters
        viewModel.forceGet(categoryId, 0, minPrice, maxPrice, selectedColors, searchContent);
        
        // Observe và update UI
        viewModel.getAllProductsByCategory(categoryId).observe(this, productModels -> {
            dataList = new ArrayList<>();
            
            for (ItemsModel product : productModels) {
                // Filter theo colors nếu có
                if (!selectedColors.isEmpty() && product.getColor() != null) {
                    boolean matchColor = false;
                    for (VariationModel color : selectedColors) {
                        for (String productColor : product.getColor()) {
                            if (productColor.toLowerCase().contains(color.name.toLowerCase())) {
                                matchColor = true;
                                break;
                            }
                        }
                        if (matchColor) break;
                    }
                    if (!matchColor) continue;
                }
                
                dataList.add(product);
            }
            
            adapter = new CatalogAdapter(this, dataList.size(), dataList);
            Log.e(TAG, "applyFilters: " + dataList.size() + " items found");
            gridView.setAdapter(adapter);
        });
    }
}
