package com.example.basketballshoesandroidshop.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.basketballshoesandroidshop.R;

import java.util.List;

public class OptionAdapter extends BaseAdapter {
    Context context;
    List<Option> optionList;
    LayoutInflater inflater;

    public OptionAdapter(Context context, List<Option> optionList) {
        this.context = context;
        this.optionList = optionList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return optionList.size();
    }

    @Override
    public Object getItem(int i) {
        return optionList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Option item = optionList.get(i);
        view = inflater.inflate(R.layout.item_option, null);
        //Get element
        TextView name = (TextView) view.findViewById(R.id.itOption_tv_name);
        RadioButton checked = (RadioButton) view.findViewById(R.id.itOption_rb_option);
        //Set value
        name.setText(item.getName());
        checked.setChecked(item.checked);

        return view;    }
}
