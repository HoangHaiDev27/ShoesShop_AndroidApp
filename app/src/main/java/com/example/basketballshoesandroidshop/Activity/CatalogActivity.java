package com.example.basketballshoesandroidshop.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.basketballshoesandroidshop.Adapter.CatalogAdapter;
import com.example.basketballshoesandroidshop.Domain.ItemsModel;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCatalogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        categoryId = intent.getIntExtra("categoryId", 1);
        categoryName = intent.getStringExtra("categoryName");


        init();
        sort = new Sort(categoryId, viewModel, edtSearch.getText().toString().trim());
        loadData();
        setUpEditText();
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
    }
}
