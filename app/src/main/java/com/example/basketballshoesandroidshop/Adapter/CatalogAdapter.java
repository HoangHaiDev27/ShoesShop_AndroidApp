package com.example.basketballshoesandroidshop.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.basketballshoesandroidshop.Domain.ItemsModel;
import com.example.basketballshoesandroidshop.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CatalogAdapter extends ArrayAdapter<ItemsModel> {

    private Context context;
    private static final String TAG = "CatalogAdapter";
    private List<ItemsModel> dataList;

    public CatalogAdapter(@NonNull Context context, int resource, @NonNull List<ItemsModel> objects) {
        super(context, resource, objects);
        this.context = context;
        this.dataList = objects;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.tvName = convertView.findViewById(R.id.tv_product_name);
            viewHolder.tvDescription = convertView.findViewById(R.id.tv_product_description);
            viewHolder.imvProduct = convertView.findViewById(R.id.imv_product_img);
            viewHolder.imvLoveLike = convertView.findViewById(R.id.iv_love_like);
            viewHolder.tvPrice = convertView.findViewById(R.id.text_price);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final ItemsModel item = dataList.get(position);
        if (item == null) {
            return convertView;
        }
        viewHolder.tvName.setText(item.getTitle());
        viewHolder.tvDescription.setText(item.getDescription());
        viewHolder.tvPrice.setText(item.getPrice()+"");
        Picasso.with(context)
                .load(item.getPicUrl().get(0))
                .into(viewHolder.imvProduct);

        return convertView;
    }

    private static class ViewHolder {
        TextView tvName, tvPrice;
        TextView tvDescription;
        ImageView imvProduct;
        ImageButton imvLoveLike;
    }
}
