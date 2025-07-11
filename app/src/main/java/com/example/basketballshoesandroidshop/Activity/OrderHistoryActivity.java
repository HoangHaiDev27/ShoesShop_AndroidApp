package com.example.basketballshoesandroidshop.Activity;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.example.basketballshoesandroidshop.Adapter.OrdersPagerAdapter;
import com.example.basketballshoesandroidshop.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class OrderHistoryActivity extends AppCompatActivity {

    private final String[] titles = new String[]{"Chờ xác nhận", "Chờ lấy hàng", "Chờ giao hàng", "Đã giao", "Đã hủy", "Trả hàng"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        OrdersPagerAdapter pagerAdapter = new OrdersPagerAdapter(this, titles);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(titles[position])
        ).attach();
        if (getIntent().hasExtra("SELECTED_TAB_INDEX")) {
            int selectedTabIndex = getIntent().getIntExtra("SELECTED_TAB_INDEX", 0);

            // In ra Logcat để kiểm tra
            Log.d("TabDebug", "Đã nhận được chỉ số tab: " + selectedTabIndex);

            viewPager.setCurrentItem(selectedTabIndex, false);
        } else {
            Log.d("TabDebug", "Không nhận được chỉ số tab nào từ Intent.");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
