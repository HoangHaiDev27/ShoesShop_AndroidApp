package com.example.basketballshoesandroidshop.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.basketballshoesandroidshop.Activity.OrderListFragment;

public class OrdersPagerAdapter extends FragmentStateAdapter {

    private final String[] titles;
    private final String userId;

    public OrdersPagerAdapter(@NonNull FragmentActivity fragmentActivity, String[] titles, String userId) {
        super(fragmentActivity);
        this.titles = titles;
        this.userId = userId; // Lưu lại userId
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Gửi trạng thái tương ứng cho mỗi fragment
        return OrderListFragment.newInstance(titles[position], userId);
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }
}
