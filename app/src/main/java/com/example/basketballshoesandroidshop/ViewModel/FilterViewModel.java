package com.example.basketballshoesandroidshop.ViewModel;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;


import com.example.basketballshoesandroidshop.Domain.CategoryModel;
import com.example.basketballshoesandroidshop.Domain.VariationModel;

import java.util.List;

public class FilterViewModel extends AndroidViewModel {
    public MutableLiveData<List<VariationModel>> selectVariations = new MutableLiveData<>();
    public MutableLiveData<List<CategoryModel>> selectCategories = new MutableLiveData<>();

    public FilterViewModel(@NonNull Application application) {
        super(application);
    }

    public void setCategories(List<CategoryModel> categoryModels) {
        selectCategories.setValue(categoryModels);
    }

    public void setVariations(List<VariationModel> variations) {
        selectVariations.setValue(variations);
    }
}

