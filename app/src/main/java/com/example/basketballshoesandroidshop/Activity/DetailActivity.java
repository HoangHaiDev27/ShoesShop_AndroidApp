package com.example.basketballshoesandroidshop.Activity;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.basketballshoesandroidshop.Adapter.ColorAdapter;
import com.example.basketballshoesandroidshop.Adapter.PicListAdapter;
import com.example.basketballshoesandroidshop.Adapter.SizeAdapter;
import com.example.basketballshoesandroidshop.Domain.ItemsModel;
import com.example.basketballshoesandroidshop.Helper.ManagmentCart;
import com.example.basketballshoesandroidshop.R;
import com.example.basketballshoesandroidshop.databinding.ActivityDetailBinding;
import com.example.basketballshoesandroidshop.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity {
    private ActivityDetailBinding binding;
    private ItemsModel object;
    private int numberOrder = 1;
    private ManagmentCart managmentCart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        managmentCart = new ManagmentCart(this);
        getBundles();
        initPicList();
        initSize();
        initColor();

    }

    private void initColor() {
        binding.RecyclerColor.setAdapter(new ColorAdapter(object.getColor()));
        binding.RecyclerColor.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
    }

    private void initSize() {
        binding.RecyclerSize.setAdapter(new SizeAdapter(object.getSize()));
        binding.RecyclerSize.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,true));
    }

    private void initPicList() {
        ArrayList<String> picList = new ArrayList<>(object.getPicUrl());
        Glide.with(this)
                .load(picList.get(0)).into(binding.pic);
        binding.picList.setAdapter(new PicListAdapter(picList,binding.pic));
        binding.picList.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
    }

    private void getBundles() {
        object = (ItemsModel) getIntent().getSerializableExtra("object");
        binding.titleItemTxt.setText(object.getTitle());
        binding.priceTxt.setText("$"+object.getPrice());
        binding.oldItemPriceTxt.setText("$"+object.getOldPrice());
        binding.oldItemPriceTxt.setPaintFlags(binding.oldItemPriceTxt.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        binding.descriptionItemTxt.setText(object.getDescription());

        binding.addToCartBtn.setOnClickListener( v ->{
            object.setNumberInCart(numberOrder);
            managmentCart.insertItem(object);
        });
        binding.backItemBtn.setOnClickListener(v->finish());

    }
}