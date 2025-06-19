package com.example.basketballshoesandroidshop.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.basketballshoesandroidshop.Domain.ItemsModel;
import com.example.basketballshoesandroidshop.Helper.ChangeNumberItemsListener;
import com.example.basketballshoesandroidshop.Helper.ManagmentCart;
import com.example.basketballshoesandroidshop.databinding.ViewholderCartBinding;

import java.util.ArrayList;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.Viewholder> {
    ArrayList<ItemsModel> listItemSelected;
    ChangeNumberItemsListener changeNumberItemsListener;
    private ManagmentCart managmentCart;

    public CartAdapter(ArrayList<ItemsModel> listItemSelected , ChangeNumberItemsListener changeNumberItemsListener, Context context) {
        this.listItemSelected = listItemSelected;
        this.changeNumberItemsListener = changeNumberItemsListener;
        managmentCart = new ManagmentCart(context);
    }

    @NonNull
    @Override
    public CartAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderCartBinding binding = ViewholderCartBinding.inflate(LayoutInflater.from(parent.getContext()));
        return new Viewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartAdapter.Viewholder holder, @SuppressLint("RecyclerView") int position) {
        holder.binding.titleTxt.setText(listItemSelected.get(position).getTitle());

        // Set individual item price
        holder.binding.feeEachitem.setText("$" + listItemSelected.get(position).getPrice());

        // Set total price per item = price * quantity (rounded)
        holder.binding.totalEachItem.setText(
                "$" + Math.round(
                        listItemSelected.get(position).getNumberInCart() * listItemSelected.get(position).getPrice()
                )
        );

        // Set quantity
        holder.binding.numberItemtxt.setText(
                String.valueOf(listItemSelected.get(position).getNumberInCart())
        );
        Glide.with(holder.itemView.getContext())
                .load(listItemSelected.get(position).getPicUrl().get(0))
                .into((holder.binding.pic));
        holder.binding.plsuCarrBtn.setOnClickListener(v -> managmentCart.plusItem(listItemSelected,position,()->{
            notifyDataSetChanged();
            changeNumberItemsListener.changed();
        })
        );

        holder.binding.minimusCart.setOnClickListener(v -> managmentCart.minusItem(listItemSelected,position,()->{
                    notifyDataSetChanged();
                    changeNumberItemsListener.changed();
                })
        );
    }

    @Override
    public int getItemCount() {
        return listItemSelected.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder{
        ViewholderCartBinding binding;
        public Viewholder(ViewholderCartBinding binding) {
            super(binding.getRoot());
            this.binding =binding;
        }
    }
}
