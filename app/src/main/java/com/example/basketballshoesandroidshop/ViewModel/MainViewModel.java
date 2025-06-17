package com.example.basketballshoesandroidshop.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.basketballshoesandroidshop.Domain.CategoryModel;
import com.example.basketballshoesandroidshop.Repository.MainRepository;

import java.util.ArrayList;

public class MainViewModel extends ViewModel {
    private final MainRepository repository = new MainRepository();

    public LiveData<ArrayList<CategoryModel>> loadCategory() {
        return repository.loadCategory();
    }
}
